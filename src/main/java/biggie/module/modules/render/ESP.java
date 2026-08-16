package biggie.module.modules.render;

import biggie.event.render.Render3DEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.util.render.RenderUtil;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ESP extends Module {
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

            final RenderManager renderManager = mc.getRenderManager();

            double pX = RenderUtil.interpPos(player.posX, player.lastTickPosX, ServerRotation.timer.renderPartialTicks);
            double pY = RenderUtil.interpPos(player.posY, player.lastTickPosY, ServerRotation.timer.renderPartialTicks);
            double pZ = RenderUtil.interpPos(player.posZ, player.lastTickPosZ, ServerRotation.timer.renderPartialTicks);

            if (rotate.value) {
                GL11.glPushMatrix();
                GL11.glTranslated(pX - renderManager.viewerPosX, pY - renderManager.viewerPosY, pZ - renderManager.viewerPosZ);
                GL11.glRotatef(-player.rotationYaw, 0.0f, 1.0f, 0.0f);
                GL11.glTranslated(renderManager.viewerPosX - pX, renderManager.viewerPosY - pY, renderManager.viewerPosZ - pZ);
            }

            if (lineBox.value)
                RenderUtil.drawOutlinedBoundingBox(lastBoundingBox, boundingBox, 1.6f, lR.value.intValue(), lG.value.intValue(), lB.value.intValue());

            if (filledBox.value)
                RenderUtil.drawBoundingBox(lastBoundingBox, boundingBox, fR.value.intValue(), fG.value.intValue(), fB.value.intValue(), fillOpacity.value.intValue());

            if (rotate.value)
                GL11.glPopMatrix();
        }
    }
}
