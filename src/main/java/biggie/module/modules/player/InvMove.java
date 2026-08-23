package biggie.module.modules.player;

import biggie.event.input.PlayerInputEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.gui.inventory.GuiInventory;
import org.lwjgl.input.Keyboard;

public class InvMove extends Module {
	public InvMove() {
		super("InvMove", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
	}

	@EventTarget
	public void onPlayerInputEvent(PlayerInputEvent event) {
		if (event.getType() != EnumEventType.POST)
			return;

		if (mc.theWorld == null && mc.thePlayer == null)
			return;

		if (!(mc.currentScreen instanceof GuiInventory))
			return;

		event.forward = 0.0f;
		event.strafe = 0.0f;

		if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()))
			++event.forward;

		if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()))
			--event.forward;

		if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()))
			++event.strafe;

		if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()))
			--event.strafe;
	}
}
