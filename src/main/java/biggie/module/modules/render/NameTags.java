package biggie.module.modules.render;

import biggie.event.render.Render3DEvent;
import biggie.event.render.RenderEntityNameTagEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.util.render.RenderUtil;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

// TODO: Adicionar e implementar isso
public class NameTags extends Module {
    public NameTags() {
        super("NameTags", ModuleCategory.RENDER, Keyboard.KEY_NONE);
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

            drawPlayerNametag(player);
        }
    }

    @EventTarget
    public void onRenderEntityNameTag(RenderEntityNameTagEvent event) {
        if (!(event.en instanceof EntityPlayer) || !mc.theWorld.playerEntities.contains(event.en) || event.en == mc.thePlayer || event.en.isDead)
            return;

        event.setCancelled(true);
    }

    void drawPlayerNametag(final EntityPlayer enPlayer) {
        final Vec3 lastPos = new Vec3(enPlayer.lastTickPosX, enPlayer.lastTickPosY + enPlayer.getEyeHeight() + 0.5, enPlayer.lastTickPosZ);
        final Vec3 pos = new Vec3(enPlayer.posX, enPlayer.posY + enPlayer.getEyeHeight() + 0.5, enPlayer.posZ);

        final RenderManager renderManager = mc.getRenderManager();
        final float distanceScale = mc.thePlayer.getDistanceToEntity(enPlayer) / 12.5f;
        final float scale = 0.016666668f * 1.6f * distanceScale;

        final double pX = RenderUtil.interpPos(pos.xCoord, lastPos.xCoord, ServerRotation.timer.renderPartialTicks);
        final double pY = RenderUtil.interpPos(pos.yCoord, lastPos.yCoord, ServerRotation.timer.renderPartialTicks);
        final double pZ = RenderUtil.interpPos(pos.zCoord, lastPos.zCoord, ServerRotation.timer.renderPartialTicks);

        GL11.glPushMatrix();

        GL11.glTranslated(pX - renderManager.viewerPosX, pY - renderManager.viewerPosY, pZ - renderManager.viewerPosZ);

        GL11.glRotatef(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);

        GL11.glScaled(scale, scale, scale);

        RenderUtil.drawWorldRect(new Vec3(0, 0, 0), new Vec3(0, 0, 0), mc.fontRendererObj.getStringWidth(enPlayer.getDisplayNameString()) + 2, mc.fontRendererObj.FONT_HEIGHT + 2, 0, 0, 0, 100);

        GL11.glRotatef(-renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        GL11.glRotatef(renderManager.playerViewY, 0.0f, 1.0f, 0.0f);

        GL11.glScaled(1.0f / scale, 1.0f / scale, 1.0f / scale);

        GL11.glTranslated(renderManager.viewerPosX - pX, renderManager.viewerPosY - pY, renderManager.viewerPosZ - pZ);

        RenderUtil.drawWorldText(
                lastPos,
                pos,
                enPlayer.getDisplayNameString(),
                255,
                255,
                255,
                255,
                scale
        );

        GL11.glPopMatrix();
    }
}
