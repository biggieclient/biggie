package byteware.util.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class RotationUtil {
    public static float[] getRotationTo(EntityPlayer from, final double x, final double y, final double z) {
        final double relX = x - from.posX;
        final double relY = y - from.posY - from.getEyeHeight();
        final double relZ = z - from.posZ;

        final double dist = Math.sqrt((relX * relX) + (relZ * relZ));

        final float yaw = MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(relZ, relX)));
        final float pitch = -MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(relY, dist)));

        return new float[] { yaw, pitch };
    }

    public static double rayCastToBoundingBox(
            final double startX, final double startY, final double startZ,
            final double minX, final double maxX,
            final double minY, final double maxY,
            final double minZ, final double maxZ,
            final float yaw, final float pitch
    ) {
        double relMaxX = maxX - startX;
        double relMinX = minX - startX;

        double relMaxZ = maxZ - startZ;
        double relMinZ = minZ - startZ;

        double relMaxY = maxY - startY;
        double relMinY = minY - startY;

        final double radYaw = Math.toRadians(yaw);
        final double radPitch = Math.toRadians(-pitch);

        final double pitchFactor = Math.cos(radPitch);

        final double xDir = -Math.sin(radYaw) * pitchFactor;
        final double zDir = Math.cos(radYaw) * pitchFactor;
        final double yDir = Math.sin(radPitch);

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

        if ((tMinX > tMaxZ) || (tMinZ > tMaxX) || (tMinY > tMaxX) || (tMinX > tMaxY) || (tMinY > tMaxZ) || (tMinZ > tMaxY))
            return -1;

        final double minIntersect = Math.max(Math.max(tMinX, tMinZ), tMinY);
        final double maxIntersect = Math.min(Math.min(tMaxX, tMaxZ), tMaxY);

        if (maxIntersect < 0)
            return -1;

        return Math.max(0, minIntersect);
    }
}
