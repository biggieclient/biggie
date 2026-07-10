package byteware.event.network;

import net.lenni0451.asmevents.event.wrapper.CancellableEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class ReceivePacketEvent extends CancellableEvent {
	public final Packet<?> packet;

	public ReceivePacketEvent(Packet<INetHandler> packet) {
		this.packet = packet;
	}
}
