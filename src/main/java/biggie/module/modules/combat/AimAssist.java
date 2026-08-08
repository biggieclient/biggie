package biggie.module.modules.combat;

import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.util.player.RotationUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AimAssist extends Module {
	private final BooleanSetting horizontal = new BooleanSetting(
			"Horizontal",
			true
	);

	private final BooleanSetting vertical = new BooleanSetting(
			"Vertical",
			true
	);

	private final DoubleSetting range = new DoubleSetting(
			"Range",
			4.0,
			3.0,
			6.0,
			0.01
	);

	private final DoubleSetting fov = new DoubleSetting(
			"FOV",
			45,
			45,
			360,
			5
	);

	private final DoubleSetting smooth = new DoubleSetting(
			"Smooth",
			5,
			1,
			10,
			0.5
	);

	private final DoubleSetting speed = new DoubleSetting(
			"Speed",
			5,
			1,
			10,
			0.5
	);

	private final BooleanSetting clickOnly = new BooleanSetting(
			"Click Only",
			true
	);

	private final BooleanSetting throughBlocks = new BooleanSetting(
			"Through Blocks",
			false
	);

	private Entity target = null;
	private float[] rots = null;

	public AimAssist() {
		super("AimAssist", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		target = null;
		rots = null;
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRenderTick() {
		if (mc.theWorld == null || mc.thePlayer == null) {
			target = null;
			rots = null;
			return;
		}

		final double sqRange = range.value * range.value;

		if (clickOnly.value && !Mouse.isButtonDown(mc.gameSettings.keyBindAttack.getKeyCode() + 100))
			return;

		final float lastYaw = mc.thePlayer.rotationYaw;
		final float lastPitch = mc.thePlayer.rotationPitch;

		for (Entity entity : mc.theWorld.loadedEntityList) {
			if (!(entity instanceof EntityLivingBase))
				continue;

			if (entity == mc.thePlayer)
				continue;

			if (entity.isDead)
				continue;

			if (mc.thePlayer.getDistanceSqToEntity(entity) > sqRange)
				continue;

			final float[] enRots = RotationUtil.getRotationTo(
					mc.thePlayer,
					entity.posX,
					entity.posY + entity.getEyeHeight() * 0.5,
					entity.posZ
			);

			if (!RotationUtil.isInFOV(lastYaw, enRots[0], fov.value.floatValue()))
				continue;

			rots = enRots;
			target = entity;

			// esse break serve pra pegar o primeiro player que achar, se fizermos um switch vamos ter que tirar.
			break;
		}

		if (target == null)
			return;

		final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;

		final float deltaYaw = rots[0] - lastYaw;
		final float patchedDeltaYaw = MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);

		final float yaw = lastYaw + patchedDeltaYaw;

		final float deltaPitch = rots[1] - lastPitch;
		final float patchedDeltaPitch = MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(Math.round(deltaPitch / gcd) * gcd), -90, 90);

		final float pitch = lastPitch + patchedDeltaPitch;

		if (horizontal.value)
			mc.thePlayer.rotationYaw = (float) RenderUtil.interpPos(yaw, lastYaw, (float) ((speed.value) / (smooth.value * 10)));

		if (vertical.value)
			mc.thePlayer.rotationPitch = (float) RenderUtil.interpPos(pitch, lastPitch, (float) ((speed.value) / (smooth.value * 10)));

		target = null;
		rots = null;
	}
}
