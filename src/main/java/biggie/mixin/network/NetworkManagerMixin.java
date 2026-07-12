package biggie.mixin.network;

import biggie.addons.NetworkManagerAddon;
import biggie.event.network.ReceivePacketEvent;
import biggie.event.network.SendPacketEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin implements NetworkManagerAddon {

	@Shadow
	@Final
	private ReentrantReadWriteLock readWriteLock;

	@Shadow
	protected abstract void dispatchPacket(Packet inPacket, GenericFutureListener<? extends Future<? super Void>>[] futureListeners);

	@Shadow
	protected abstract void flushOutboundQueue();

	@Shadow
	public abstract boolean isChannelOpen();

	@Shadow
	@Final
	private Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue;

	@Shadow
	private INetHandler packetListener;

	@Shadow
	private Channel channel;

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

	@Override
	public void biggie$sendPacketNoEvent(Packet packet) {
		if (this.isChannelOpen()) {
			this.flushOutboundQueue();
			this.dispatchPacket(packet, null);
		} else {
			this.readWriteLock.writeLock().lock();

			try {
				this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, (GenericFutureListener<? extends Future<? super Void>>) null));
			} finally {
				this.readWriteLock.writeLock().unlock();
			}
		}
	}
}
