package biggie.module.modules.render;

import biggie.event.render.Render3DEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.util.player.RotationUtil;
import biggie.util.render.RenderUtil;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

// TODO: Adicionar e implementar isso
public class Nametags extends Module {
    public Nametags() {
        super("Nametags", ModuleCategory.RENDER, Keyboard.KEY_NONE);
    }
    
    @EventTarget(noParamEvents = Render3DEvent.class)
    public void onRender3D() {
        final Vec3 lastPos = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight() + 0.5, mc.thePlayer.lastTickPosZ);
        final Vec3 pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + 0.5, mc.thePlayer.posZ);

        final RenderManager renderManager = mc.getRenderManager();
        final float scale = 0.016666668f * 1.6f;

        final double pX = RenderUtil.interpPos(pos.xCoord, lastPos.xCoord, ServerRotation.timer.renderPartialTicks);
        final double pY = RenderUtil.interpPos(pos.yCoord, lastPos.yCoord, ServerRotation.timer.renderPartialTicks);
        final double pZ = RenderUtil.interpPos(pos.zCoord, lastPos.zCoord, ServerRotation.timer.renderPartialTicks);

        GL11.glPushMatrix();

        GL11.glTranslated(pX - renderManager.viewerPosX, pY - renderManager.viewerPosY, pZ - renderManager.viewerPosZ);
        GL11.glRotatef(
                -RotationUtil.getInterpYaw(mc.thePlayer.prevRotationYaw, mc.thePlayer.rotationYaw),
                0.0f,
                1.0f,
                0.0f
        );
        GL11.glScaled(scale, scale, scale);

        RenderUtil.drawWorldRect(new Vec3(0, 0, 0), new Vec3(0, 0, 0), mc.fontRendererObj.getStringWidth(mc.thePlayer.getName()) + 2, mc.fontRendererObj.FONT_HEIGHT + 2, 0, 0, 0, 100);

        GL11.glScaled(1.0f / scale, 1.0f / scale, 1.0f / scale);
        GL11.glTranslated(renderManager.viewerPosX - pX, renderManager.viewerPosY - pY, renderManager.viewerPosZ - pZ);

        RenderUtil.drawWorldText(
                lastPos,
                pos,
                mc.thePlayer.getName(),
                -RotationUtil.getInterpYaw(mc.thePlayer.prevRotationYaw, mc.thePlayer.rotationYaw),
                255,
                255,
                255,
                255,
                0.016666668f * 1.6f
        );

        GL11.glPopMatrix();
    }
}
