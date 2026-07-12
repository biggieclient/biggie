package biggie.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

public class RenderUtil {
	public static double interpPos(double destPos, double lastTickPos, float partialTicks) {
		return lastTickPos + (destPos - lastTickPos) * partialTicks;
	}

	public static AxisAlignedBB getLastTickBoundingBox(EntityLivingBase en) {
		double minZ = en.lastTickPosZ - en.width * 0.5;
		double maxZ = en.lastTickPosZ + en.width * 0.5;

		double minX = en.lastTickPosX - en.width * 0.5;
		double maxX = en.lastTickPosX + en.width * 0.5;

		double minY = en.lastTickPosY;
		double maxY = en.lastTickPosY + en.height;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static void drawBoundingBox(
			double x, double y, double z,
			double x2, double y2, double z2,
			double lastX, double lastY, double lastZ,
			double lastX2, double lastY2, double lastZ2,
			int r, int g, int b, int a
	) {
		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer worldRenderer = tess.getWorldRenderer();

		final double interpMinX = interpPos(x, lastX, ServerRotation.timer.renderPartialTicks);
		final double interpMinY = interpPos(y, lastY, ServerRotation.timer.renderPartialTicks);
		final double interpMinZ = interpPos(z, lastZ, ServerRotation.timer.renderPartialTicks);

		final double interpMaxX = interpPos(x2, lastX2, ServerRotation.timer.renderPartialTicks);
		final double interpMaxY = interpPos(y2, lastY2, ServerRotation.timer.renderPartialTicks);
		final double interpMaxZ = interpPos(z2, lastZ2, ServerRotation.timer.renderPartialTicks);

		final double minX = interpMinX - renderManager.viewerPosX;
		final double maxX = interpMaxX - renderManager.viewerPosX;

		final double minY = interpMinY - renderManager.viewerPosY;
		final double maxY = interpMaxY - renderManager.viewerPosY;

		final double minZ = interpMinZ - renderManager.viewerPosZ;
		final double maxZ = interpMaxZ - renderManager.viewerPosZ;

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glDisable(GL11.GL_CULL_FACE);

		worldRenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);

		worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();

		worldRenderer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

		tess.draw();

		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
	}
}
