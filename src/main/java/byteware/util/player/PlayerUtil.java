package byteware.util.player;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerUtil {
    public static double GetPlayerTickSpeed(EntityPlayer player) {
        return Math.sqrt((player.motionX * player.motionX) + (player.motionY * player.motionY) + (player.motionZ * player.motionZ));
    }

    public static double GetPlayerSpeed(EntityPlayer player) {
        final double relX = player.motionX * 20;
        final double relY = player.motionY * 20;
        final double relZ = player.motionZ * 20;

        return Math.sqrt((relX * relX) + (relY * relY) + (relZ * relZ));
    }
}
