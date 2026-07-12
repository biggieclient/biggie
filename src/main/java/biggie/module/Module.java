package biggie.module;

import biggie.setting.Setting;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;

public class Module {
	public final String name;
	public final ModuleCategory category;
	public int keybind;
	private boolean enabled = false;

	protected final Minecraft mc = Minecraft.getMinecraft();

	public final ArrayList<Setting<?>> settings = new ArrayList<>();

	public Module(String name, ModuleCategory category, int keybind) {
		this.name = name;
		this.category = category;
		this.keybind = keybind;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;

		if (enabled) {
			this.onEnable();

			EventManager.register(this);
		} else {
			EventManager.unregister(this);

			this.onDisable();
		}
	}

	public void registerSettings(Setting<?>... settings) {
		this.settings.addAll(Arrays.asList(settings));
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	public void toggle() {
		setEnabled(!enabled);
	}
}
