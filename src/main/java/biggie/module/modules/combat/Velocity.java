package biggie.module.modules.combat;

import biggie.event.motion.LivingUpdateEvent;
import biggie.event.network.ReceivePacketEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
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
			"Jump"
	);

	public boolean receivedDamage = false;

	public Velocity() {
		super("Velocity", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		receivedDamage = false;
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

		if (mc.gameSettings.keyBindJump.isKeyDown()) {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
			receivedDamage = false;

			return;
		}

		KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
	}
}
