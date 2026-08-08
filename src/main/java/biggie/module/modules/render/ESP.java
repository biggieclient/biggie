package biggie.module.modules.render;

import biggie.event.render.Render3DEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class ESP extends Module {
    private final BooleanSetting lineBox = new BooleanSetting("Line Box", true);
    private final BooleanSetting filledBox = new BooleanSetting("Filled Box", false);

    public ESP() {
        super("ESP", ModuleCategory.RENDER, Keyboard.KEY_NONE);
    }

    @EventTarget(noParamEvents = Render3DEvent.class)
    public void onRender3D() {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer)
                continue;

            if (player.isDead)
                continue;

            final AxisAlignedBB lastBoundingBox = RenderUtil.getBoundingBox(
                    player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ,
                    player.width, player.height
            );
            final AxisAlignedBB boundingBox = RenderUtil.getBoundingBox(
                    player.posX, player.posY, player.posZ,
                    player.width, player.height
            );

            if (lineBox.value)
                RenderUtil.drawOutlinedBoundingBox(lastBoundingBox, boundingBox, 1.6f, 255, 255, 255);

            if (filledBox.value)
                RenderUtil.drawBoundingBox(lastBoundingBox, boundingBox, (int) (255 * 0.75f), (int) (255 * 0.75f), (int) (255 * 0.75f), 45);
        }
    }
}
