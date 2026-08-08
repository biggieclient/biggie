package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.network.SendPacketEvent;
import biggie.event.render.Render3DEvent;
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
	private final IntegerSetting delay = new IntegerSetting("Delay", 100, 10, 1000, 1);
	private final DoubleSetting range = new DoubleSetting("Range", 4.0, 3.0, 6.0, 0.01);

	private final CopyOnWriteArrayList<Packet<?>> packets = new CopyOnWriteArrayList<>();
	private Vec3 lastPos = null;

	private long lastMs = 0;
	private boolean shouldLag = false;
	private boolean attacking = false;

	public LagRange() {
		super("LagRange", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		for (Packet<?> packet : packets) {
			PacketUtil.sendPacketNoEvent(packet);
		}

		packets.clear();

		lastPos = null;

		lastMs = 0;

		shouldLag = false;
		attacking = false;
	}

	@EventTarget
	public void onAttack(AttackEvent event) {
		if (event.getType() == EnumEventType.PRE) {
			attacking = true;
		}
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() == EnumEventType.PRE) {
			if (mc.theWorld != null && mc.thePlayer != null) {
				for (Entity entity : mc.theWorld.loadedEntityList) {
					if (entity instanceof EntityLivingBase) {
						EntityLivingBase en = (EntityLivingBase) entity;

						if (en == mc.thePlayer) {
							continue;
						}

						if (mc.thePlayer.getDistanceToEntity(en) <= range.value) {
							if (attacking) {
								if (shouldLag) {
									shouldLag = false;
								}

								attacking = false;
							} else {
								shouldLag = true;
							}
						}
					}
				}
			}
		}
	}

	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() == EnumEventType.PRE) {
			if (shouldLag) {
				if (!packets.isEmpty()) {
					if (System.currentTimeMillis() - lastMs >= delay.value) {
						for (Packet<?> packet : packets) {
							PacketUtil.sendPacketNoEvent(packet);
						}

						packets.clear();

						lastPos = mc.thePlayer.getPositionVector();
						lastMs = System.currentTimeMillis();
					}
				}
			} else {
				if (!packets.isEmpty()) {
					if (System.currentTimeMillis() - lastMs >= delay.value) {
						for (Packet<?> packet : packets) {
							PacketUtil.sendPacketNoEvent(packet);
						}

						packets.clear();

						lastPos = null;
						lastMs = 0;
					}
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

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		lastPos = null;
		shouldLag = false;
		attacking = false;

		packets.clear();
	}

	@EventTarget
	public void onSendPacket(SendPacketEvent event) {
		if (shouldLag) {
			if (event.packet.getClass().getSimpleName().startsWith("C")) {
				packets.add(event.packet);

				event.setCancelled(true);
			}
		}
	}
}
