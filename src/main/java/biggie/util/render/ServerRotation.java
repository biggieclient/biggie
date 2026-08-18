package biggie.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ServerRotation {
	public static float DEST_YAW;
	public static float DEST_PITCH;

	public static float INTERP_YAW;
	public static float INTERP_PITCH;

	public static float LAST_TICK_YAW;
	public static float LAST_TICK_PITCH;

	public static Timer timer = null;

	public static void setup() {
		timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
	}

	public static float getInterpYaw(final float progress) {
		return LAST_TICK_YAW + MathHelper.wrapAngleTo180_float(INTERP_YAW - LAST_TICK_YAW) * progress;
	}

	public static float getInterpPitch(final float progress) {
		return LAST_TICK_PITCH + MathHelper.wrapAngleTo180_float(INTERP_PITCH - LAST_TICK_PITCH) * progress;
	}

	public static float getInterpYaw() {
		return INTERP_YAW + MathHelper.wrapAngleTo180_float(DEST_YAW - INTERP_YAW) * (0.325f);
	}

	public static float getInterpPitch() {
		return INTERP_PITCH + MathHelper.wrapAngleTo180_float(DEST_PITCH - INTERP_PITCH) * (0.325f);
	}
}
