package biggie.util.player;

import biggie.util.math.MathUtil;
import biggie.util.render.ServerRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class RotationUtil {
	public static float[] getRotationTo(final EntityPlayer from, final Vec3 vec) {
		final double dX = vec.xCoord - from.posX;
		final double dY = vec.yCoord - from.posY - from.getEyeHeight();
		final double dZ = vec.zCoord - from.posZ;

		final double module = MathUtil.getModule(dX, 0, dZ);

		final float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
		final float pitch = (float) -Math.toDegrees(Math.atan2(dY, module));

		return new float[]{yaw, pitch};
	}

	public static float[] getRotationTo(final EntityPlayer from, final double x, final double y, final double z) {
		final double dX = x - from.posX;
		final double dY = y - from.posY - from.getEyeHeight();
		final double dZ = z - from.posZ;

		final double module = MathUtil.getModule(dX, 0, dZ);

		final float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
		final float pitch = (float) -Math.toDegrees(Math.atan2(dY, module));

		return new float[]{yaw, pitch};
	}

	public static float getInterpRot(final float yaw, final float destYaw, final float progress) {
		final float patchedDeltaYaw = MathHelper.wrapAngleTo180_float(destYaw - yaw);
		return yaw + patchedDeltaYaw * progress;
	}

	public static float getInterpRot(final float yaw, final float destYaw) {
		final float patchedDeltaYaw = MathHelper.wrapAngleTo180_float(destYaw - yaw);
		return yaw + patchedDeltaYaw * ServerRotation.timer.renderPartialTicks;
	}

	public static float getGCDPatchedYaw(final Minecraft mc, final float yaw, final float destYaw) {
		final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;
		return yaw + MathHelper.wrapAngleTo180_float(MathUtil.alignToInterval(destYaw - yaw, gcd));
	}

	public static float getGCDPatchedPitch(final Minecraft mc, final float pitch, final float destPitch) {
		final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;
		final float gcdPatchedPitch = pitch + MathHelper.wrapAngleTo180_float(MathUtil.alignToInterval(destPitch - pitch, gcd));

		return MathHelper.clamp_float(gcdPatchedPitch, -90, 90);
	}

	public static boolean isInFOV(final float yaw, final float destYaw, final float fov) {
		final float deltaYaw = MathHelper.wrapAngleTo180_float(destYaw - yaw);
		final float halfFOV = fov * 0.5f;

		return deltaYaw <= halfFOV && deltaYaw >= -halfFOV;
	}

	public static MovingObjectPosition rayTrace(final EntityPlayer from, final World world, final float yaw, final float pitch, final double rayDistance) {
		final double radYaw = Math.toRadians(yaw);
		final double radPitch = Math.toRadians(-pitch);

		final double pitchFactor = Math.cos(radPitch);

		final double xDir = -Math.sin(radYaw) * pitchFactor;
		final double zDir = Math.cos(radYaw) * pitchFactor;
		final double yDir = Math.sin(radPitch);

		return world.rayTraceBlocks(
				new Vec3(from.posX, from.posY + from.getEyeHeight(), from.posZ),
				new Vec3(from.posX + xDir * rayDistance, from.posY + from.getEyeHeight() + yDir * rayDistance, from.posZ + zDir * rayDistance)
		);
	}

	public static Vec3 getVectorForRotation(final float yaw, final float pitch) {
		final float radPitch = (float) Math.toRadians(-pitch);
		final float radYaw = (float) Math.toRadians(yaw + 90);

		final float xDir = MathHelper.cos(radYaw);
		final float yDir = MathHelper.sin(radPitch);
		final float zDir = MathHelper.sin(radYaw);

		final float pitchFactor = MathHelper.cos(radPitch);

		return new Vec3(xDir * pitchFactor, yDir, zDir * pitchFactor);
	}

	public static double getBestTargetRelY(final EntityPlayer from, final EntityLivingBase target) {
		final double playerEyeY = from.posY + from.getEyeHeight();

		final double targetFeetY = 0;
		final double targetChestY = target.height * 0.55;
		final double targetHeadY = target.getEyeHeight();

		if (playerEyeY >= targetHeadY)
			return targetHeadY;
		else if (playerEyeY <= targetFeetY)
			return targetFeetY;
		else
			return targetChestY;
	}

	public static MovingObjectPosition rayTraceAll(
			final EntityPlayer from,
			final World world,
			final float yaw,
			final float pitch,
			final double rayDistance,
			final float partialTicks,
			final boolean checkBlocks
	) {
		final Vec3 lookVec = getVectorForRotation(yaw, pitch);

		final Vec3 eyePos = from.getPositionEyes(partialTicks);
		final Vec3 rayVec = eyePos.add(new Vec3(lookVec.xCoord * rayDistance, lookVec.yCoord * rayDistance, lookVec.zCoord * rayDistance));

		final AxisAlignedBB intersectBox = from.getEntityBoundingBox()
				.addCoord(lookVec.xCoord * rayDistance, lookVec.yCoord * rayDistance, lookVec.zCoord * rayDistance)
				.expand(1.0, 1.0, 1.0);
		final List<Entity> enList = world.getEntitiesWithinAABBExcludingEntity(from, intersectBox);

		double closestDist = Double.NaN;
		MovingObjectPosition closestRayTrace = null;

		final MovingObjectPosition blockRayTrace =
				checkBlocks ? world.rayTraceBlocks(eyePos, rayVec, false, false, true)
						: null;

		if (blockRayTrace != null) {
			closestDist = eyePos.distanceTo(blockRayTrace.hitVec);
			closestRayTrace = blockRayTrace;
		}

		for (final Entity en : enList) {
			if (!en.canBeCollidedWith())
				continue;

			final float borderSize = en.getCollisionBorderSize();
			final AxisAlignedBB boundingBox = en.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);

			if (boundingBox.isVecInside(eyePos)) {
				if (!Double.isNaN(closestDist) && closestDist <= 0.0)
					continue;

				closestDist = 0.0;
				closestRayTrace = new MovingObjectPosition(en, eyePos);
				continue;
			}

			final MovingObjectPosition enRayTrace = boundingBox.calculateIntercept(eyePos, rayVec);

			if (enRayTrace == null)
				continue;

			final double dist = eyePos.distanceTo(enRayTrace.hitVec);

			if (!Double.isNaN(closestDist) && dist >= closestDist)
				continue;

			closestDist = dist;
			closestRayTrace = new MovingObjectPosition(en, enRayTrace.hitVec);
		}

		return closestRayTrace;
	}

	public static MovingObjectPosition rayTraceAll(
			final EntityPlayer from,
			final World world,
			final float yaw,
			final float pitch,
			final double rayDistance,
			final boolean checkBlocks
	) {
		return rayTraceAll(from, world, yaw, pitch, rayDistance, 1.0f, checkBlocks);
	}

	// WARNING: Isso não é uma gambiarra, mas meio que isso deve gastar processamento para um caralho ja que
	// faz um dot product pra cada combinação de movement pra achar o vetor mais parecido com o do client usando o yaw do server.
	public static float[] getFixedMove(final EntityPlayer from, final float clientYaw, final float serverYaw, final float clientForward, final float clientStrafe) {
		if (clientForward == 0 && clientStrafe == 0)
			return new float[]{0, 0};

		final float[] clientMove = MathUtil.getMovementFromYawAndInput(clientYaw, clientForward, clientStrafe, 1.0f);

		float clientX = clientMove[0];
		float clientZ = clientMove[1];

		final float clientInvSize = 1.0f / MathUtil.getModule(clientX, 0, clientZ);

		clientX *= clientInvSize;
		clientZ *= clientInvSize;

		float closestDot = Float.NaN;
		float closestForward = 0;
		float closestStrafe = 0;

		for (int forward = -1; forward <= 1; ++forward) {
			for (int strafe = -1; strafe <= 1; ++strafe) {
				if (forward == 0 && strafe == 0)
					continue;

				final float[] tryMove = MathUtil.getMovementFromYawAndInput(serverYaw, forward, strafe, 1.0f);

				final float serverX = tryMove[0];
				final float serverZ = tryMove[1];

				final float invSize = 1.0f / MathUtil.getModule(serverX, 0, serverZ);

				final float currDot = ((serverX * invSize) * clientX) + ((serverZ * invSize) * clientZ);

				if (currDot > closestDot || Double.isNaN(closestDot)) {
					closestForward = forward;
					closestStrafe = strafe;
					closestDot = currDot;
				}
			}
		}

		final float factor = from.isSneaking() ? 0.3f : 1.0f;
		return new float[]{closestForward * factor, closestStrafe * factor};
	}
}
