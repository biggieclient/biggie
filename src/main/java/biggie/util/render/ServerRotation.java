package biggie.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ServerRotation {
	public static float DEST_YAW;
	public static float DEST_PITCH;

	public static float LAST_TICK_YAW;
	public static float LAST_TICK_PITCH;

	public static boolean ROTATE_BODY = false;

	private static Timer timer = null;

	public static void setup() {
		timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer");
	}

	public static float interpYaw(final float destYaw, final float lastTickYaw) {
		return lastTickYaw + MathHelper.wrapAngleTo180_float(destYaw - lastTickYaw) * timer.renderPartialTicks;
	}

	public static float getInterpYaw() {
		return LAST_TICK_YAW + MathHelper.wrapAngleTo180_float(DEST_YAW - LAST_TICK_YAW) * timer.renderPartialTicks;
	}

	public static float getInterpPitch() {
		return LAST_TICK_PITCH + MathHelper.wrapAngleTo180_float(DEST_PITCH - LAST_TICK_PITCH) * timer.renderPartialTicks;
	}
}
