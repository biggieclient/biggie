package biggie.util.player;

import biggie.util.AbstractUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovementInput;

public class MovementUtil extends AbstractUtil {
	public static boolean isMoving() {
		return mc.thePlayer.movementInput.moveForward != 0 && mc.thePlayer.movementInput.moveStrafe != 0;
	}
}
