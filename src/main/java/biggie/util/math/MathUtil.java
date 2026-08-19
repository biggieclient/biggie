package biggie.util.math;

import net.minecraft.util.Vec3;

public class MathUtil {
    public static double getSqModule(final Vec3 vec) {
        return (vec.xCoord * vec.xCoord) + (vec.yCoord * vec.yCoord) + (vec.zCoord * vec.zCoord);
    }

    public static double getSqModule(final double x, final double y, final double z) {
        return (x * x) + (y * y) + (z * z);
    }

    public static double getSqModule(final float x, final float y, final float z) {
        return (x * x) + (y * y) + (z * z);
    }

    public static double getModule(final Vec3 vec) {
        return Math.sqrt((vec.xCoord * vec.xCoord) + (vec.yCoord * vec.yCoord) + (vec.zCoord * vec.zCoord));
    }

    public static double getModule(final double x, final double y, final double z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public static float getModule(final float x, final float y, final float z) {
        return (float) Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public static double alignToInterval(final double value, final double interval) {
        return Math.round(value / interval) * interval;
    }

    public static float alignToInterval(final float value, final float interval) {
        return Math.round(value / interval) * interval;
    }

    public static float[] getMovementFromYawAndInput(final float yaw, final float forward, final float strafe, final float speed) {
        final float radYaw = (float) Math.toRadians(yaw);

        final float dX = (float) (Math.cos(radYaw) * strafe - Math.sin(radYaw) * forward);
        final float dZ = (float) (Math.cos(radYaw) * forward + Math.sin(radYaw) * strafe);

        return new float[] { dX * speed, dZ * speed };
    }

    public static double[] getMovementFromYawAndInput(final float yaw, final double forward, final double strafe, final double speed) {
        final double radYaw = Math.toRadians(yaw);

        final double dX = Math.cos(radYaw) * strafe - Math.sin(radYaw) * forward;
        final double dZ = Math.cos(radYaw) * forward + Math.sin(radYaw) * strafe;

        return new double[] { dX * speed, dZ * speed };
    }

    public static float getLinearStep(final float a, final float b, final float stepSize) {
        final float diff = b - a;
        final float absDiff = Math.abs(diff);

        if (absDiff <= 0.01f)
            return 0.0f;

        final float sign = diff / absDiff;
        final float linearStep = sign * stepSize;

        if (Math.abs(linearStep) > absDiff)
            return diff;

        return linearStep;
    }
}
