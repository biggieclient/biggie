package biggie.module.modules.movement;

import biggie.event.motion.LivingUpdateEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.math.MathUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import org.lwjgl.input.Keyboard;

public class Speed extends Module {
	public Speed() {
		super("Speed", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE);
	}

	private final ListSetting mode = new ListSetting("Mode", "Strafe", "Strafe");
	private final DoubleSetting speed = new DoubleSetting("Blocks P/ Tick", 0.2, 0.1, 1, 0.1);
	private final DoubleSetting jumpMotion = new DoubleSetting("Jump Motion", 0.42, 0.01, 1, 0.01);

	@EventTarget
	public void onTick(LivingUpdateEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.thePlayer == null || mc.theWorld == null)
			return;

		if (mode.value.equals("Strafe")) {
			double moveForward = mc.thePlayer.movementInput.moveForward;
			double moveStrafe = mc.thePlayer.movementInput.moveStrafe;

			if (moveStrafe == 0 && moveForward == 0)
				return;

			final double invDist = 1.0f / MathUtil.getModule(moveStrafe, 0, moveForward);

			moveForward *= invDist;
			moveStrafe *= invDist;

			final double[] move = MathUtil.getMovementFromYawAndInput(mc.thePlayer.rotationYaw, moveForward, moveStrafe, speed.value);

			mc.thePlayer.motionX = move[0];
			mc.thePlayer.motionZ = move[1];

			if (mc.thePlayer.onGround)
				mc.thePlayer.motionY = jumpMotion.value;
		}
	}
}
