package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.motion.LivingUpdateEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.ListSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import org.lwjgl.input.Keyboard;

public class MoreKB extends Module {
	private final ListSetting mode = new ListSetting(
			"Mode",
			"Legit",
			"Legit"
	);

	private boolean attacked = false;

	@Override
	public String getInfo() {
		return mode.value;
	}

	public MoreKB() {
		super("MoreKnockback", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		attacked = false;
	}

	@EventTarget
	public void onAttack(AttackEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		attacked = true;
	}

	@EventTarget()
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (!attacked)
			return;

		if (event.getType() != EnumEventType.PRE)
			return;

		if (mode.value.equals("Legit")) {
			mc.thePlayer.sprintingTicksLeft = 1;
			attacked = false;
		}
	}
}
