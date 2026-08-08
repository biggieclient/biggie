package biggie.module.modules.combat;

import biggie.event.input.PostPlayerInputEvent;
import biggie.event.motion.JumpEvent;
import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.StrafeEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.util.player.ChatUtil;
import biggie.util.player.RotationUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class KillAura extends Module {
	private float yaw = Float.NaN;
	private float pitch = Float.NaN;

	private EntityPlayer target = null;
	private long lastAttack = 0;

	private final DoubleSetting aps = new DoubleSetting("APS", 20, 1, 20, 0.5);

	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		if (!Float.isNaN(yaw)) {
			final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;
			final float deltaYaw = mc.thePlayer.rotationYaw - yaw;

			mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
		}

		yaw = Float.NaN;
		pitch = Float.NaN;
		target = null;
	}

	@EventTarget
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		EntityPlayer finalPlayer = null;

		for (Entity en : mc.theWorld.getLoadedEntityList()) {
			if (!(en instanceof EntityPlayer))
				continue;

			if (en == mc.thePlayer)
				continue;

			EntityPlayer enPlayer = (EntityPlayer) en;

			if (mc.thePlayer.getDistanceSqToEntity(enPlayer) > 16)
				continue;

			finalPlayer = enPlayer;
			break;
		}

		final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;

		if (finalPlayer == null) {
			if (!Float.isNaN(yaw)) {
				final float deltaYaw = mc.thePlayer.rotationYaw - yaw;

				mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
				yaw = Float.NaN;
			}

			target = null;
			return;
		}

		float lastYaw = (Float.isNaN(yaw)) ? mc.thePlayer.rotationYaw : yaw;
		float lastPitch = (Float.isNaN(yaw)) ? mc.thePlayer.rotationPitch : pitch;

		final float[] rots = RotationUtil.getRotationTo(
				mc.thePlayer,
				finalPlayer.posX, finalPlayer.posY + finalPlayer.getEyeHeight() * 0.65, finalPlayer.posZ
		);

		final AxisAlignedBB boundingBox = finalPlayer.getEntityBoundingBox();

		final double intersect = RotationUtil.rayCastToBoundingBox(
				mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
				boundingBox.minX, boundingBox.minY, boundingBox.minZ,
				boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ,
				rots[0], rots[1]
		);
		final boolean canAttack = intersect <= 3.0 && intersect != -1;

		ChatUtil.addMessage("Min Intersect: " + intersect);

		if (!canAttack) {
			if (!Float.isNaN(yaw)) {
				final float deltaYaw = mc.thePlayer.rotationYaw - yaw;

				mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
				yaw = Float.NaN;
			}

			target = null;
			return;
		}

		final float deltaYaw = rots[0] - lastYaw;
		final float deltaPitch = rots[1] - lastPitch;

		yaw = lastYaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
		pitch = MathHelper.clamp_float(lastPitch + Math.round(deltaPitch / gcd) * gcd, -90, 90);

		target = finalPlayer;
	}

	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (Float.isNaN(yaw) || Float.isNaN(pitch) || target == null)
			return;

		final long currTime = System.currentTimeMillis();

		if (currTime - lastAttack > 1000 / aps.value) {
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, target);
			lastAttack = currTime;
		}

		event.yaw = yaw;
		event.pitch = pitch;
	}

	@EventTarget
	public void onStrafe(StrafeEvent event) {
		if (Float.isNaN(yaw))
			return;

		event.yaw = yaw;
	}

	@EventTarget
	public void onJump(JumpEvent event) {
		if (Float.isNaN(yaw))
			return;

		event.yaw = yaw;
	}

	@EventTarget
	public void onPostPlayerInput(PostPlayerInputEvent event) {
		if (Float.isNaN(yaw))
			return;

		final float[] fixedMove = RotationUtil.getFixedMove(
				mc.thePlayer.rotationYaw, yaw,
				mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe
		);

		event.moveForward = fixedMove[0];
		event.moveStrafe = fixedMove[1];
	}
}
