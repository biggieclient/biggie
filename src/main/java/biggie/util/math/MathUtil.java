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

    public static double getModule(final float x, final float y, final float z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    public static double alignToInterval(final double value, final double interval) {
        return Math.round(value / interval) * interval;
    }

    public static float alignToInterval(final float value, final float interval) {
        return Math.round(value / interval) * interval;
    }
}
