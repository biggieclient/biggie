package biggie.module.modules.movement;

import biggie.event.client.GameLoopEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {
	public Sprint() {
		super("Sprint", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		if (mc.gameSettings.keyBindSprint.isKeyDown()) {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
		}
	}

	@EventTarget(noParamEvents = GameLoopEvent.class)
	public void onGameLoop() {
		if (mc.theWorld != null && mc.thePlayer != null) {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
		}
	}
}
