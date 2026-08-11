package biggie.util.network;

import biggie.addons.NetworkManagerAddon;
import biggie.util.AbstractUtil;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;

public class PacketUtil extends AbstractUtil {
	public static void sendPacketNoEvent(Packet<?> packet) {
		((NetworkManagerAddon) mc.getNetHandler().getNetworkManager()).biggie$sendPacketNoEvent(packet);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void receivePacket(Packet packet) {
		NetHandlerPlayClient netHandler = mc.getNetHandler();

		if (netHandler.getNetworkManager().channel().isOpen()) {
			try {
				packet.processPacket(netHandler);
			} catch (ThreadQuickExitException ignored) {
			}
		}
	}
}
