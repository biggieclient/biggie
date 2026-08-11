package biggie.util.player;

import biggie.util.AbstractUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ChatUtil extends AbstractUtil {
	public static void addMessage(String message) {
		String clientTag = "[" + EnumChatFormatting.GREEN + "Biggie" + EnumChatFormatting.RESET + "] ";

		mc.thePlayer.addChatMessage(new ChatComponentText(clientTag + message));
	}
}
