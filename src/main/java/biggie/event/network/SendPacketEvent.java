package biggie.event.network;

import net.lenni0451.asmevents.event.wrapper.CancellableEvent;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class SendPacketEvent extends CancellableEvent {
	public final Packet<?> packet;

	public SendPacketEvent(Packet<INetHandler> packet) {
		this.packet = packet;
	}
}
