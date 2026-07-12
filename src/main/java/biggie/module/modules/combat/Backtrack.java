package biggie.module.modules.combat;

import biggie.event.client.AttackEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.event.network.ReceivePacketEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.IntegerSetting;
import biggie.util.network.PacketUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventPriority;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.CopyOnWriteArrayList;

public class Backtrack extends Module {
	private final IntegerSetting delay = new IntegerSetting("Delay", 100, 10, 1000, 1);

	private final CopyOnWriteArrayList<Packet<?>> packets = new CopyOnWriteArrayList<>();
	private EntityPlayer target = null;

	private long lastMs = 0;

	public Backtrack() {
		super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		target = null;

		for (Packet<?> packet : packets) {
			PacketUtil.receivePacket(packet);
		}

		packets.clear();

		lastMs = 0;
	}

	@EventTarget(priority = EnumEventPriority.HIGH)
	public void onAttack(AttackEvent event) {
		if (event.getType() == EnumEventType.POST) {
			if (target == null && event.entity instanceof EntityPlayer) {
				target = (EntityPlayer) event.entity;
			}
		}
	}

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		target = null;

		packets.clear();
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() == EnumEventType.POST) {
			if (target != null) {
				if (System.currentTimeMillis() - lastMs >= delay.value) {
					if (!packets.isEmpty()) {
						for (Packet<?> packet : packets) {
							PacketUtil.receivePacket(packet);
						}

						packets.clear();
					}

					target = null;

					lastMs = System.currentTimeMillis();
				}
			}
		}
	}

	@EventTarget
	public void onReceivePacket(ReceivePacketEvent event) {
		if (target != null) {
			if (event.packet.getClass().getSimpleName().startsWith("S")) {
				if (event.packet instanceof S14PacketEntity) {
					Entity entity = ((S14PacketEntity) event.packet).getEntity(mc.theWorld);

					if (entity == null || entity.getEntityId() != target.getEntityId()) {
						return;
					}
				}

				if (event.packet instanceof S12PacketEntityVelocity) {
					if (((S12PacketEntityVelocity) event.packet).getEntityID() != target.getEntityId()) {
						return;
					}
				}

				if (event.packet instanceof S18PacketEntityTeleport) {
					if (((S18PacketEntityTeleport) event.packet).getEntityId() != target.getEntityId()) {
						return;
					}
				}

				if (event.packet instanceof S08PacketPlayerPosLook) {
					target = null;

					for (Packet<?> packet : packets) {
						PacketUtil.receivePacket(packet);
					}

					packets.clear();

					return;
				}

				if (event.packet instanceof S3EPacketTeams) {
					return;
				}

				if (event.packet instanceof S20PacketEntityProperties) {
					return;
				}

				if (event.packet instanceof S0FPacketSpawnMob) {
					return;
				}

				if (event.packet instanceof S40PacketDisconnect) {
					return;
				}

				if (event.packet instanceof S26PacketMapChunkBulk || event.packet instanceof S21PacketChunkData) {
					return;
				}

				if (event.packet instanceof S3BPacketScoreboardObjective) {
					return;
				}

				packets.add(event.packet);

				event.setCancelled(true);
			}
		}
	}
}
