package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.client.GameLoopEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.network.SendPacketEvent;
import biggie.event.render.Render3DEvent;
import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.util.network.PacketUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.CopyOnWriteArrayList;

public class LagRange extends Module {
	private final IntegerSetting delay = new IntegerSetting(
			"Delay",
			100,
			10,
			1000,
			1
	);
	private final DoubleSetting range = new DoubleSetting(
			"Range",
			4.0,
			3.0,
			6.0,
			0.01
	);

	private final CopyOnWriteArrayList<PacketData> packets = new CopyOnWriteArrayList<>();
	private Vec3 lastPos = null;

	private boolean shouldLag = false;
	private boolean attacking = false;

	public LagRange() {
		super("LagRange", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		lastPos = null;

		shouldLag = false;
		attacking = false;

		for (PacketData packetData : packets) {
			PacketUtil.sendPacketNoEvent(packetData.packet);
		}

		packets.clear();
	}

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		lastPos = null;
		shouldLag = false;
		attacking = false;

		packets.clear();
	}

	@EventTarget
	public void onAttack(AttackEvent event) {
		if (event.getType() == EnumEventType.PRE) {
			if (!attacking) {
				attacking = true;
			}
		}
	}

	@EventTarget(noParamEvents = GameLoopEvent.class)
	public void onGameLoop() {
		if (mc.theWorld != null && mc.thePlayer != null) {
			for (Entity entity : mc.theWorld.loadedEntityList) {
				if (entity instanceof EntityLivingBase) {
					if (entity == mc.thePlayer) {
						continue;
					}

					if (entity.isDead) {
						continue;
					}

					if (mc.thePlayer.getDistanceToEntity(entity) < range.value) {
						shouldLag = !attacking;
					}
				}
			}

			attacking = false;
		}
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRenderTick() {
		if (!packets.isEmpty()) {
			for (PacketData packetData : packets) {
				if (System.currentTimeMillis() - packetData.receiveTime >= delay.value) {
					PacketUtil.sendPacketNoEvent(packetData.packet);

					packets.remove(packetData);
				}
			}
		}
	}

	@EventTarget(noParamEvents = Render3DEvent.class)
	public void onRender3D() {
		if (lastPos != null) {
			RenderUtil.drawBoundingBox(
					lastPos.xCoord,
					lastPos.yCoord,
					lastPos.zCoord,
					lastPos.xCoord + mc.thePlayer.width,
					lastPos.yCoord + mc.thePlayer.height,
					lastPos.zCoord + mc.thePlayer.width,
					lastPos.xCoord,
					lastPos.yCoord,
					lastPos.zCoord,
					lastPos.xCoord + mc.thePlayer.width,
					lastPos.yCoord + mc.thePlayer.height,
					lastPos.zCoord + mc.thePlayer.width,
					0,
					255,
					0,
					50
			);
		}
	}

	@EventTarget
	public void onSendPacket(SendPacketEvent event) {
		if (shouldLag) {
			if (event.packet.getClass().getSimpleName().startsWith("C")) {
				packets.add(new PacketData(System.currentTimeMillis(), event.packet));

				event.setCancelled(true);
			}
		}
	}

	static class PacketData {
		public final long receiveTime;
		public final Packet<?> packet;

		public PacketData(long receiveTime, Packet<?> packet) {
			this.receiveTime = receiveTime;
			this.packet = packet;
		}
	}
}
