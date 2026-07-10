package byteware.module.modules.movement;

import byteware.event.motion.ItemSlowDownEvent;
import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.BooleanSetting;
import byteware.setting.settings.DoubleSetting;
import net.lenni0451.asmevents.event.EventTarget;
import org.lwjgl.input.Keyboard;

public class NoItemSlow extends Module {
	private final DoubleSetting forward = new DoubleSetting("Forward", 1.0, 0.2, 1.0, 0.05);
	private final DoubleSetting strafe = new DoubleSetting("Strafe", 1.0, 0.2, 1.0, 0.05);
	private final BooleanSetting allowSprinting = new BooleanSetting("Allow Sprinting", true);

	public NoItemSlow() {
		super("No Item Slow", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE);
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
