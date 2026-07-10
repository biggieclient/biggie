package byteware.module.modules.combat;

import byteware.event.client.AttackEvent;
import byteware.event.motion.LivingUpdateEvent;
import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.ListSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class MoreKB extends Module {
	private final ListSetting mode = new ListSetting(
			"Mode",
			"LegitFast",
			"LegitFast"
	);

	private boolean attacked = false;

	public MoreKB() {
		super("MoreKB", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		attacked = false;

		if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
		}
	}

	@EventTarget
	public void onAttack(AttackEvent event) {
		if (event.getType() == EnumEventType.PRE) {
			attacked = true;
		}
	}

	@EventTarget()
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (attacked) {
			if (event.getType() == EnumEventType.PRE) {
				if (mode.value.equals("LegitFast")) {
					mc.thePlayer.sprintingTicksLeft = 1;

					attacked = false;
				}
			}
		}
	}
}
