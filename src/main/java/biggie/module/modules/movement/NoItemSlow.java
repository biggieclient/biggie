package biggie.module.modules.movement;

import biggie.event.motion.ItemSlowDownEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import net.lenni0451.asmevents.event.EventTarget;
import org.lwjgl.input.Keyboard;

public class NoItemSlow extends Module {
	private final DoubleSetting forward = new DoubleSetting("Forward", 1.0, 0.2, 1.0, 0.05);
	private final DoubleSetting strafe = new DoubleSetting("Strafe", 1.0, 0.2, 1.0, 0.05);
	private final BooleanSetting allowSprinting = new BooleanSetting("Allow Sprinting", true);

	public NoItemSlow() {
		super("NoItemSlow", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE);
	}

	@EventTarget
	public void onItemSlowDown(ItemSlowDownEvent event) {
		event.forward = forward.value.floatValue();
		event.strafe = strafe.value.floatValue();

		if (!event.sprint) {
			event.sprint = allowSprinting.value;
		}
	}
}
