package biggie.util.player;

import biggie.util.math.MathUtil;
import biggie.util.render.ServerRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class RotationUtil {
	public static float[] getRotationTo(final EntityPlayer from, final Vec3 vec) {
		final double dX = vec.xCoord - from.posX;
		final double dY = vec.yCoord - from.posY - from.getEyeHeight();
		final double dZ = vec.zCoord - from.posZ;

		final double module = MathUtil.getModule(dX, 0, dZ);

		final float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
		final float pitch = (float) -Math.toDegrees(Math.atan2(dY, module));

		return new float[] { yaw, pitch };
	}

	public static float[] getRotationTo(final EntityPlayer from, final double x, final double y, final double z) {
		final double dX = x - from.posX;
		final double dY = y - from.posY - from.getEyeHeight();
		final double dZ = z - from.posZ;

		final double module = MathUtil.getModule(dX, 0, dZ);

		final float yaw = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90;
		final float pitch = (float) -Math.toDegrees(Math.atan2(dY, module));

		return new float[] { yaw, pitch };
	}

	public static float getInterpYaw(final float yaw, final float destYaw, final float progress) {
		final float patchedDeltaYaw = MathHelper.wrapAngleTo180_float(destYaw - yaw);
		return yaw + patchedDeltaYaw * progress;
	}

	public static float getInterpYaw(final float yaw, final float destYaw) {
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
				new Vec3(from.posX + xDir * rayDistance, from.posY + from.getEyeHeight() + yDir * rayDistance, from.posZ  + zDir * rayDistance)
		);
	}

	public static double rayCastToBoundingBox(
			final double startX, final double startY, final double startZ,
			final double minX, final double minY, final double minZ,
			final double maxX, final double maxY, final double maxZ,
			final float yaw, final float pitch

	) {
		double relMaxX = maxX - startX;
		double relMinX = minX - startX;

		double relMaxZ = maxZ - startZ;
		double relMinZ = minZ - startZ;

		double relMaxY = maxY - startY;
		double relMinY = minY - startY;

		final double rYaw = Math.toRadians(yaw);
		final double rPitch = Math.toRadians(-pitch);

		final double pitchFactor = Math.cos(rPitch);

		final double xDir = -Math.sin(rYaw) * pitchFactor;
		final double zDir = Math.cos(rYaw) * pitchFactor;
		final double yDir = Math.sin(rPitch);

		if (xDir < 0) {
			final double temp = relMinX;
			relMinX = relMaxX;
			relMaxX = temp;
		}

		if (yDir < 0) {
			final double temp = relMinY;
			relMinY = relMaxY;
			relMaxY = temp;

		}

		if (zDir < 0) {
			final double temp = relMinZ;
			relMinZ = relMaxZ;
			relMaxZ = temp;
		}

		final double invXDir = 1 / xDir;
		final double invZDir = 1 / zDir;
		final double invYDir = 1 / yDir;

		final double tMaxX = relMaxX * invXDir;
		final double tMinX = relMinX * invXDir;

		final double tMaxZ = relMaxZ * invZDir;
		final double tMinZ = relMinZ * invZDir;

		final double tMaxY = relMaxY * invYDir;
		final double tMinY = relMinY * invYDir;

		final double minIntersect = Math.max(Math.max(tMinX, tMinZ), tMinY);
		final double maxIntersect = Math.min(Math.min(tMaxX, tMaxZ), tMaxY);

		if (minIntersect > maxIntersect)
			return -1;

		if (maxIntersect < 0)
			return -1;

		return Math.max(0, minIntersect);
	}

	// WARNING: Isso não chega a ser uma gambiarra, mas meio que isso deve gastar processamento para um caralho ja que
	// faz um dot product pra cada combinação de movement pra achar o vetor mais parecido com o do client usando o yaw do server.
	public static float[] getFixedMove(float clientYaw, float serverYaw, float  clientForward, float clientStrafe) {
		if (clientForward == 0 && clientStrafe == 0)
			return new float[] { 0, 0 };

		final float serverRadYaw = (float) Math.toRadians(serverYaw);
		final float clientRadYaw = (float) Math.toRadians(clientYaw);

		float clientX = ((float) -Math.sin(clientRadYaw) * clientForward) + ((float) Math.cos(clientRadYaw) * clientStrafe);
		float clientZ = ((float) Math.cos(clientRadYaw) * clientForward) + ((float) Math.sin(clientRadYaw) * clientStrafe);

		final float clientInvSize = 1 / (float) Math.sqrt((clientX * clientX) + (clientZ * clientZ));

		clientX *= clientInvSize;
		clientZ *= clientInvSize;

		float finalDot = Float.NaN;
		float finalForward = 0;
		float finalStrafe = 0;

		for (int forward = -1; forward <= 1; ++forward) {
			for (int strafe = -1; strafe <= 1; ++strafe) {
				if (forward == 0 && strafe == 0)
					continue;

				final float dX = ((float) -Math.sin(serverRadYaw) * forward) + ((float) Math.cos(serverRadYaw) * strafe);
				final float dZ = ((float) Math.cos(serverRadYaw) * forward) + ((float) Math.sin(serverRadYaw) * strafe);

				final float invSize = 1 / (float) Math.sqrt((dX * dX) + (dZ * dZ));

				final float currDot = ((dX * invSize) * clientX) + ((dZ * invSize) * clientZ);

				if (currDot > finalDot || Double.isNaN(finalDot)) {
					finalForward = forward;
					finalStrafe = strafe;
					finalDot = currDot;
				}
			}
		}

		return new float[] { finalForward, finalStrafe };
	}
}
