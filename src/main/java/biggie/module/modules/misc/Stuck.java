package biggie.module.modules.misc;

import biggie.event.motion.StrafeEvent;
import biggie.event.motion.UpdateEvent;
import biggie.event.network.SendPacketEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

public class Stuck extends Module {
	private double motionX = 0.0;
	private double motionY = 0.0;
	private double motionZ = 0.0;

	public Stuck() {
		super("Stuck", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}

	public void onEnable() {
		motionX = mc.thePlayer.motionX;
		motionY = mc.thePlayer.motionY;
		motionZ = mc.thePlayer.motionZ;

		mc.thePlayer.motionX = 0.0;
		mc.thePlayer.motionY = 0.0;
		mc.thePlayer.motionZ = 0.0;
	}

	public void onDisable() {
		mc.thePlayer.motionX = motionX;
		mc.thePlayer.motionY = motionY;
		mc.thePlayer.motionZ = motionZ;

		motionX = 0.0;
		motionY = 0.0;
		motionZ = 0.0;
	}

	@EventTarget
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		mc.thePlayer.motionX = 0.0;
		mc.thePlayer.motionY = 0.0;
		mc.thePlayer.motionZ = 0.0;
	}

	@EventTarget
	public void onSendPacket(SendPacketEvent event) {
		if (!(event.packet instanceof C03PacketPlayer))
			return;

		event.setCancelled(true);
	}

	@EventTarget
	public void onStrafe(StrafeEvent event) {
		event.forward = 0.0f;
		event.strafe = 0.0f;
	}
}
