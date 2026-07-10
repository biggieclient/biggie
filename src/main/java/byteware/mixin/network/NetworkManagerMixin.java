package byteware.mixin.network;

import byteware.event.network.ReceivePacketEvent;
import byteware.event.network.SendPacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin {

	@Inject(
			method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	public void channelRead0_callReceivePacketEvent(ChannelHandlerContext p_channelRead0_1_, Packet<INetHandler> p_channelRead0_2_, CallbackInfo ci) {
		ReceivePacketEvent receivePacketEvent = new ReceivePacketEvent(p_channelRead0_2_);

		if (EventManager.call(receivePacketEvent).isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(
			method = "sendPacket(Lnet/minecraft/network/Packet;)V",
			at = @At("HEAD"),
			cancellable = true
	)
	public void sendPacket_callSendPacketEvent(Packet<INetHandler> packetIn, CallbackInfo ci) {
		SendPacketEvent sendPacketEvent = new SendPacketEvent(packetIn);

		if (EventManager.call(sendPacketEvent).isCancelled()) {
			ci.cancel();
		}
	}
}
