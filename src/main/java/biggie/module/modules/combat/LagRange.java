package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.event.network.SendPacketEvent;
import biggie.event.render.Render3DEvent;
import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.util.network.PacketUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

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
	private final BooleanSetting rotate = new BooleanSetting("Rotate", true);

	private final DoubleSetting fillOpacity = new DoubleSetting("Fill Opacity", 45, 20, 100, 5);

	private final DoubleSetting lR = new DoubleSetting("Line Red", 255, 0, 255, 5);
	private final DoubleSetting lG = new DoubleSetting("Line Green", 255, 0, 255, 5);
	private final DoubleSetting lB = new DoubleSetting("Line Blue", 255,  0, 255, 5);

	private final DoubleSetting fR = new DoubleSetting("Fill Red", 255, 0, 255, 5);
	private final DoubleSetting fG = new DoubleSetting("Fill Green", 255, 0, 255, 5);
	private final DoubleSetting fB = new DoubleSetting("Fill Blue", 255,  0, 255, 5);

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
		return delay.value.toString();
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

		shouldLag = false;

		if (mc.thePlayer.movementInput.moveForward == 0 && mc.thePlayer.movementInput.moveStrafe == 0) {
			flushPackets();
			return;
		}

		for (Entity entity : mc.theWorld.loadedEntityList) {
			if (!(entity instanceof EntityLivingBase))
				continue;

			if (entity == mc.thePlayer)
				continue;

			if (entity.isDead)
				continue;

			if (mc.thePlayer.getDistanceSqToEntity(entity) < minRange.value * minRange.value) {
				shouldLag = false;
				break;
			}

			if (mc.thePlayer.getDistanceSqToEntity(entity) > maxRange.value * maxRange.value)
				continue;

			shouldLag = true;
		}

		if (!shouldLag)
			flushPackets();
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRenderTick() {
		if (packets.isEmpty())
			return;

		final long currTime = System.currentTimeMillis();

		for (PacketData packetData : packets) {
			if (currTime - packetData.receiveTime >= delay.value) {
				if (packetData.packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
					final C03PacketPlayer.C04PacketPlayerPosition C04 = (C03PacketPlayer.C04PacketPlayerPosition) packetData.packet;

					lastPos = serverPos;
					posTime = currTime;
					serverPos = new Vec3(C04.getPositionX(), C04.getPositionY(), C04.getPositionZ());
				}

				if (packetData.packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
					final C03PacketPlayer.C06PacketPlayerPosLook C04 = (C03PacketPlayer.C06PacketPlayerPosLook) packetData.packet;

					lastPos = serverPos;
					posTime = currTime;
					serverPos = new Vec3(C04.getPositionX(), C04.getPositionY(), C04.getPositionZ());
				}

				PacketUtil.sendPacketNoEvent(packetData.packet);
				packets.remove(packetData);
			}
		}
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

		final RenderManager renderManager = mc.getRenderManager();

		double pX = RenderUtil.interpPos(serverPos.xCoord, fixedLastPos.xCoord, progress);
		double pY = RenderUtil.interpPos(serverPos.yCoord, fixedLastPos.yCoord, progress);
		double pZ = RenderUtil.interpPos(serverPos.zCoord, fixedLastPos.zCoord, progress);

		if (rotate.value) {
			GL11.glPushMatrix();
			GL11.glTranslated(pX - renderManager.viewerPosX, pY - renderManager.viewerPosY, pZ - renderManager.viewerPosZ);
			GL11.glRotatef(-mc.thePlayer.rotationYaw, 0.0f, 1.0f, 0.0f);
			GL11.glTranslated(renderManager.viewerPosX - pX, renderManager.viewerPosY - pY, renderManager.viewerPosZ - pZ);
		}

		if (lineBox.value)
			RenderUtil.drawOutlinedBoundingBox(lastBoundingBox, boundingBox, 1.6f, lR.value.intValue(), lG.value.intValue(), lB.value.intValue(), progress);

		if (filledBox.value)
			RenderUtil.drawBoundingBox(lastBoundingBox, boundingBox, fR.value.intValue(), fG.value.intValue(), fB.value.intValue(), fillOpacity.value.intValue(), progress);

		if (rotate.value)
			GL11.glPopMatrix();
	}

	@EventTarget
	public void onSendPacket(SendPacketEvent event) {
		if (!shouldLag)
			return;

		if (!event.packet.getClass().getSimpleName().startsWith("C"))
			return;

		if (event.packet instanceof C02PacketUseEntity) {
			final C02PacketUseEntity C02 = (C02PacketUseEntity) event.packet;

			if (C02.getAction() == C02PacketUseEntity.Action.ATTACK)
				flushPackets();
		}

		packets.add(new PacketData(System.currentTimeMillis(), event.packet));
		event.setCancelled(true);
	}

	void flushPackets() {
		if (packets.isEmpty())
			return;

		synchronized (packets) {
			for (final PacketData packet : packets) {
				PacketUtil.sendPacketNoEvent(packet.packet);
			}

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
