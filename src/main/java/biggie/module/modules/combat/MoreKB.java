package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.motion.LivingUpdateEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.ListSetting;
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
