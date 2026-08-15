package biggie.util.player;

import biggie.util.AbstractUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;

public class MovementUtil extends AbstractUtil {
	public static boolean isMoving() {
		MovementInput movementInput = mc.thePlayer.movementInput;

		return movementInput.moveForward != 0 && movementInput.moveStrafe != 0;
	}

	public static double getPlayerTickSpeed(EntityPlayer player) {
		return Math.sqrt((player.motionX * player.motionX) + (player.motionY * player.motionY) + (player.motionZ * player.motionZ));
	}

	public static double getPlayerSpeed(EntityPlayer player) {
		final double relX = player.motionX * 20;
		final double relY = player.motionY * 20;
		final double relZ = player.motionZ * 20;

		return Math.sqrt((relX * relX) + (relY * relY) + (relZ * relZ));
	}
}
