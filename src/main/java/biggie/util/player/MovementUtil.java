package biggie.util.player;

import biggie.util.AbstractUtil;

public class MovementUtil extends AbstractUtil {
	public static boolean isMoving() {
		return mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0;
	}
}
