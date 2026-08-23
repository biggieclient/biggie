package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.event.network.SendPacketEvent;
import biggie.event.render.Render3DEvent;
import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.module.modules.misc.AntiBot;
import biggie.module.modules.render.ArrayListModule;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.util.network.PacketUtil;
import biggie.util.player.MovementUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class LagRange extends Module {
	private final IntegerSetting delay = new IntegerSetting(
			"Delay",
			100,
			10,
			1000,
			1
	);
	private final DoubleSetting maxRange = new DoubleSetting(
			"Max Range",
			4.0,
			3.0,
			6.0,
			0.01
	);
	private final DoubleSetting minRange = new DoubleSetting(
			"Min Range",
			4.0,
			2.0,
			6.0,
			0.01
	);

	private final BooleanSetting lineBox = new BooleanSetting("Line Box", true);
	private final BooleanSetting filledBox = new BooleanSetting("Filled Box", false);

	private final DoubleSetting fillOpacity = new DoubleSetting("Fill Opacity", 45, 20, 100, 5);

	private final CopyOnWriteArrayList<PacketData> packets = new CopyOnWriteArrayList<>();

	private Vec3 serverPos = null;
	private Vec3 lastPos = null;

	private boolean shouldLag = false;

	private long posTime = 0;

	public LagRange() {
		super("LagRange", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public String getInfo() {
		return delay.value.toString() + "ms";
	}

	@Override
	public void onDisable() {
		shouldLag = false;
		posTime = 0;
		flushPackets();
	}

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		lastPos = null;
		shouldLag = false;

		packets.clear();
	}

	@EventTarget(priority = EnumEventPriority.HIGHEST)
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null)
			return;

		if (!packets.isEmpty()) {
			final long currTime = System.currentTimeMillis();

			packets.removeIf(packetData -> {
				if (currTime - packetData.receiveTime < delay.value)
					return false;

				if (packetData.packet instanceof C03PacketPlayer) {
					final C03PacketPlayer C03 = (C03PacketPlayer) packetData.packet;

					if (C03.isMoving()) {
						lastPos = serverPos != null ? serverPos : new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
						serverPos = new Vec3(C03.getPositionX(), C03.getPositionY(), C03.getPositionZ());
						posTime = currTime;
					}
				}

				PacketUtil.sendPacketNoEvent(packetData.packet);
				return true;
			});
		}

		shouldLag = false;

		if (!MovementUtil.isMoving()) {
			flushPackets();
			return;
		}

		final double sqMinRange = minRange.value * minRange.value;
		final double sqMaxRange = maxRange.value * maxRange.value;

		for (final Entity en : mc.theWorld.loadedEntityList) {
			if (!(en instanceof EntityPlayer))
				continue;

			if (en == mc.thePlayer)
				continue;

			if (en.isDead)
				continue;

			if (AntiBot.botList.contains(en))
				continue;

			if (mc.thePlayer.getDistanceSqToEntity(en) < sqMinRange) {
				shouldLag = false;
				break;
			}

			if (mc.thePlayer.getDistanceSqToEntity(en) > sqMaxRange)
				continue;

			shouldLag = true;
		}

		if (!shouldLag)
			flushPackets();
	}

	@EventTarget(noParamEvents = Render3DEvent.class)
	public void onRender3D() {
		if (!shouldLag || serverPos == null)
			return;

		final float progress = (posTime == 0) ? 1.0f : Math.min(1, (float) (System.currentTimeMillis() - posTime) / 200.0f);
		final Vec3 fixedLastPos = lastPos == null ? serverPos : lastPos;

		final AxisAlignedBB boundingBox = RenderUtil.getBoundingBox(
				serverPos.xCoord,
				serverPos.yCoord,
				serverPos.zCoord,
				mc.thePlayer.width,
				mc.thePlayer.height
		);
		final AxisAlignedBB lastBoundingBox = RenderUtil.getBoundingBox(
				fixedLastPos.xCoord,
				fixedLastPos.yCoord,
				fixedLastPos.zCoord,
				mc.thePlayer.width,
				mc.thePlayer.height
		);

		float colorProgress = mc.thePlayer.ticksExisted * ArrayListModule.INV_TICKS;
		colorProgress -= (int) colorProgress;

		final Color[] colors = ArrayListModule.getColors(ArrayListModule.COLOR.value);

		final Color fillColor = RenderUtil.getInterpolatedColor(colors[0], colors[1], colors[0], colorProgress);
		final Color outlineColor = fillColor.darker();

		if (lineBox.value)
			RenderUtil.drawOutlinedBoundingBox(lastBoundingBox, boundingBox, 1.6f, outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), progress);

		if (filledBox.value)
			RenderUtil.drawBoundingBox(lastBoundingBox, boundingBox, fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillOpacity.value.intValue(), progress);
	}

	@EventTarget
	public void onSendPacket(SendPacketEvent event) {
		if (!shouldLag)
			return;

		if (!event.packet.getClass().getSimpleName().startsWith("C"))
			return;

		if (event.packet instanceof C02PacketUseEntity) {
			final C02PacketUseEntity C02 = (C02PacketUseEntity) event.packet;

			if (C02.getAction() == C02PacketUseEntity.Action.ATTACK) {
				flushPackets();
				return;
			}
		}

		packets.add(new PacketData(System.currentTimeMillis(), event.packet));
		event.setCancelled(true);
	}

	void flushPackets() {
		if (packets.isEmpty())
			return;

		synchronized (packets) {
			for (final PacketData packet : packets)
				PacketUtil.sendPacketNoEvent(packet.packet);

			packets.clear();
		}

		lastPos = null;
		serverPos = null;
		posTime = 0;
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
