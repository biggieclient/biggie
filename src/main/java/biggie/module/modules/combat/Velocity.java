package biggie.module.modules.combat;

import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.UpdateEvent;
import biggie.event.network.ReceivePacketEvent;
import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.IntegerSetting;
import biggie.setting.settings.ListSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {
	private final ListSetting mode = new ListSetting(
			"Mode",
			"Jump",
			"Jump",
			"MinemenAir"
	);
	private final IntegerSetting airTicks = new IntegerSetting(
			"Air Ticks",
			2,
			1,
			5,
			1,
			() -> mode.value.equals("MinemenAir")
	);

	public boolean receivedDamage = false;
	private int ticksInAir = 1;

	public Velocity() {
		super("Velocity", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		receivedDamage = false;
		ticksInAir = 0;
	}

	@Override
	public String getInfo() {
		return mode.value;
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRenderTick() {
		if (mc.theWorld != null && mc.thePlayer != null) {
			if (receivedDamage) {
				if (mode.value.equals("MinemenAir")) {
					if (!mc.thePlayer.onGround) {
						ticksInAir++;
					}

					if (ticksInAir >= airTicks.value) {
						mc.thePlayer.motionX = 0;
						mc.thePlayer.motionZ = 0;

						ticksInAir = 0;
						receivedDamage = false;
					}
				}
			}
		} else {
			if (receivedDamage) {
				receivedDamage = false;
			}

			ticksInAir = 0;
		}
	}

	@EventTarget
	public void onReceivePacket(ReceivePacketEvent event) {
		if (event.packet instanceof S12PacketEntityVelocity && !event.isCancelled()) {
			if (((S12PacketEntityVelocity) event.packet).getEntityID() == mc.thePlayer.getEntityId()) {
				receivedDamage = true;
			}
		}
	}

	@EventTarget
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.getType() != EnumEventType.PRE || !receivedDamage)
			return;

		if (mode.value.equals("Jump")) {
			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
				receivedDamage = false;

				return;
			}

			KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
		}
	}
}