package byteware.module.modules.movement;

import byteware.event.client.TickEvent;
import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.DoubleSetting;
import byteware.setting.settings.ListSetting;
import net.lenni0451.asmevents.event.EventTarget;
import org.lwjgl.input.Keyboard;

public class Speed extends Module {
    public Speed() {
        super("Speed", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE);
    }

    private final ListSetting mode = new ListSetting("Mode", "Strafe", "Strafe");
    private final DoubleSetting speed = new DoubleSetting("Blocks P/ Tick", 0.2, 0.1, 1, 0.1);
    private final DoubleSetting jumpMotion = new DoubleSetting("Jump Motion", 0.42, 0.01, 1, 0.01);

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (mode.value.equals("Strafe")) {
            double radYaw = Math.toRadians(mc.thePlayer.rotationYaw);

            double moveForward = mc.thePlayer.movementInput.moveForward;
            double moveStrafe = mc.thePlayer.movementInput.moveStrafe;

            if (moveStrafe == 0 && moveForward == 0)
                return;

            if (mc.thePlayer.onGround)
                mc.thePlayer.motionY = jumpMotion.value;

            final double invDist = 1 / Math.sqrt((moveForward * moveForward) + (moveStrafe * moveStrafe));

            moveForward *= invDist;
            moveStrafe *= invDist;

            mc.thePlayer.motionX = (-Math.sin(radYaw) * speed.value * moveForward) + (Math.cos(radYaw) * speed.value * moveStrafe);
            mc.thePlayer.motionZ = (Math.cos(radYaw) * speed.value * moveForward) + (Math.sin(radYaw) * speed.value * moveStrafe);
        }
    }
}
