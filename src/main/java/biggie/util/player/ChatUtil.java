package biggie.util.player;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class ChatUtil {
	public static void addMessage(String message) {
		String clientTag = "[" + EnumChatFormatting.GREEN + "Biggie" + EnumChatFormatting.RESET + "] ";

		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(clientTag + message));
	}
}
