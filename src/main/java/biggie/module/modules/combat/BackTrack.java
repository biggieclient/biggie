package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.event.network.ReceivePacketEvent;
import biggie.event.render.Render3DEvent;
import biggie.manager.ModuleManager;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.module.modules.render.ArrayListModule;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.util.math.MathUtil;
import biggie.util.network.PacketUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BackTrack extends Module {
	private final IntegerSetting delay = new IntegerSetting("Delay", 100, 10, 1000, 1);
	private final IntegerSetting targetFlushDelay = new IntegerSetting("Target Flush Delay", 100, 100, 1000, 50);

	private final BooleanSetting distanceCheck = new BooleanSetting("Distance Check", true);

	private final BooleanSetting cancelPong = new BooleanSetting("Cancel Pong", true);
	private final BooleanSetting cancelKeepAlive = new BooleanSetting("Cancel Keep Alive", true);

	private final BooleanSetting lineBox = new BooleanSetting("Line Box", true);
	private final BooleanSetting filledBox = new BooleanSetting("Filled Box", false);

	private final DoubleSetting fillOpacity = new DoubleSetting("Fill Opacity", 45, 20, 100, 5);

	private final CopyOnWriteArrayList<PacketData> packets = new CopyOnWriteArrayList<>();
	private EntityLivingBase target = null;

	private final LinkedHashMap<EntityLivingBase, PosData> posCache = new LinkedHashMap<>();

	private long lastAttack = 0;

	public BackTrack() {
		super("BackTrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		target = null;
		flushPackets();
	}

	@Override
	public String getInfo() {
		return delay.value.toString() + "ms";
	}

	@EventTarget(priority = EnumEventPriority.HIGHEST)
	public void onAttack(AttackEvent event) {
		if (event.getType() != EnumEventType.POST)
			return;

		if (event.entity instanceof EntityLivingBase) {
			target = (EntityLivingBase) event.entity;
			lastAttack = System.currentTimeMillis();
		}
	}

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		target = null;
		flushPackets();
	}

	@EventTarget(priority = EnumEventPriority.HIGHEST)
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null)
			return;

		final long currTime = System.currentTimeMillis();

		if (target != null && (currTime - lastAttack > targetFlushDelay.value)) {
			target = null;
			flushPackets();
		}

		if (distanceCheck.value && posCache.containsKey(target)) {
			final PosData pos = posCache.get(target);

			final double dX = pos.x - mc.thePlayer.posX;
			final double dY = pos.y - mc.thePlayer.posY;
			final double dZ = pos.z - mc.thePlayer.posZ;

			final double cacheDist = MathUtil.getSqModule(dX, dY, dZ);

			if (cacheDist < mc.thePlayer.getDistanceSq(target.posX, target.posY, target.posZ)) {
				target = null;
				flushPackets();
			}
		}

		if (target == null)
			return;

		for (final PacketData data : packets) {
			if (currTime - data.receiveTime < delay.value)
				continue;

			if (data.packet instanceof S12PacketEntityVelocity) {
				final S12PacketEntityVelocity S12 = (S12PacketEntityVelocity) data.packet;

				if (S12.getEntityID() == mc.thePlayer.getEntityId())
					ModuleManager.getModule(Velocity.class).receivedDamage = true;
			}

			PacketUtil.receivePacket(data.packet);
			packets.remove(data);
		}
	}

	@EventTarget(noParamEvents = Render3DEvent.class)
	public void onRender3D() {
		if (target == null)
			return;

		if (posCache.containsKey(target)) {
			final PosData pos = posCache.get(target);

			final float progress = (pos.posTime == 0) ? 1.0f : Math.min(1, (float) (System.currentTimeMillis() - pos.posTime) / 200.0f);

			final AxisAlignedBB boundingBox = RenderUtil.getBoundingBox(pos.x, pos.y, pos.z, target.width, target.height);
			final AxisAlignedBB lastBoundingBox = RenderUtil.getBoundingBox(pos.lastX, pos.lastY, pos.lastZ, target.width, target.height);

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
	}

	@EventTarget(priority = EnumEventPriority.HIGH)
	public void onReceivePacket(ReceivePacketEvent event) {
		if (target == null)
			return;

		final long currTime = System.currentTimeMillis();

		if (!event.packet.getClass().getSimpleName().startsWith("S"))
			return;

		if (
				(event.packet instanceof S00PacketKeepAlive && !cancelKeepAlive.value) ||
						event.packet instanceof S02PacketChat                                  ||
						event.packet instanceof S29PacketSoundEffect                           ||
						(event.packet instanceof S01PacketPong && !cancelPong.value)           ||
						event.packet instanceof S06PacketUpdateHealth
		)
			return;

		if (event.packet instanceof S0CPacketSpawnPlayer || event.packet instanceof S0FPacketSpawnMob) {
			return;
		}

		if (event.packet instanceof S01PacketJoinGame) {
			return;
		}

		if (event.packet instanceof S08PacketPlayerPosLook) {
			target = null;
			flushPackets();

			return;
		}

		if (event.packet instanceof S13PacketDestroyEntities) {
			final S13PacketDestroyEntities packet = (S13PacketDestroyEntities) event.packet;

			for (int id : packet.getEntityIDs()) {
				if (id != target.getEntityId())
					continue;

				target = null;
				flushPackets();
			}

			return;
		}

		if (event.packet instanceof S14PacketEntity) {
			if (event.packet instanceof S14PacketEntity.S16PacketEntityLook)
				return;

			final S14PacketEntity packet = ((S14PacketEntity) event.packet);
			final Entity entity = packet.getEntity(mc.theWorld);

			if (!(entity instanceof EntityLivingBase))
				return;

			if (entity == target) {
				final PosData data = posCache.get(entity);

				final double lastX = (data == null) ? entity.posX : data.x;
				final double lastY = (data == null) ? entity.posY : data.y;
				final double lastZ = (data == null) ? entity.posZ : data.z;

				final double posX = lastX + ((double) packet.func_149062_c() / 32.0);
				final double posY = lastY + ((double) packet.func_149061_d() / 32.0);
				final double posZ = lastZ + ((double) packet.func_149064_e() / 32.0);

				posCache.put(
						(EntityLivingBase) entity,
						new PosData(
								posX, posY, posZ,
								lastX, lastY, lastZ,
								currTime
						)
				);
			}
		}

		if (event.packet instanceof S18PacketEntityTeleport) {
			final S18PacketEntityTeleport packet = (S18PacketEntityTeleport) event.packet;
			final Entity entity = mc.theWorld.getEntityByID(packet.getEntityId());

			if (!(entity instanceof EntityLivingBase))
				return;

			if (entity == target) {
				final PosData data = posCache.get(entity);

				final double lastX = (data == null) ? entity.posX : data.x;
				final double lastY = (data == null) ? entity.posY : data.y;
				final double lastZ = (data == null) ? entity.posZ : data.z;

				final double posX = packet.getX() / 32.0;
				final double posY = packet.getY() / 32.0;
				final double posZ = packet.getZ() / 32.0;

				posCache.put(
						(EntityLivingBase) entity,
						new PosData(
								posX, posY, posZ,
								lastX, lastY, lastZ,
								currTime
						)
				);
			}
		}

		packets.add(new PacketData(currTime, event.packet));
		event.setCancelled(true);
	}

	void flushPackets() {
		synchronized (packets) {
			for (final PacketData data : packets)
				PacketUtil.receivePacket(data.packet);

			packets.clear();
		}

		posCache.clear();
	}

	static class PacketData {
		public final long receiveTime;
		public final Packet<?> packet;

		public PacketData(long receiveTime, Packet<?> packet) {
			this.receiveTime = receiveTime;
			this.packet = packet;
		}
	}

	static class PosData {
		public final double x;
		public final double y;
		public final double z;

		public final double lastX;
		public final double lastY;
		public final double lastZ;

		public final long posTime;

		public PosData(
				final double x, final double y, final double z,
				final double lastX, final double lastY, final double lastZ,
				final long posTime
		) {
			this.x = x;
			this.y = y;
			this.z = z;

			this.lastX = lastX;
			this.lastY = lastY;
			this.lastZ = lastZ;
			this.posTime = posTime;
		}
	}
}