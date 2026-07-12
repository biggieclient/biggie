package biggie.util.network;

import biggie.addons.NetworkManagerAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;

public class PacketUtil {
	public static void sendPacket(Packet<?> packet) {
		Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(packet);
	}

	public static void sendPacketNoEvent(Packet<?> packet) {
		((NetworkManagerAddon) Minecraft.getMinecraft().getNetHandler().getNetworkManager()).biggie$sendPacketNoEvent(packet);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void receivePacket(Packet packet) {
		NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();

		if (netHandler.getNetworkManager().channel().isOpen()) {
			try {
				packet.processPacket(netHandler);
			} catch (ThreadQuickExitException ignored) {
			}
		}
	}
}
