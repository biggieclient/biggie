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
	public static double interpPos(double destPos, double lastTickPos, float progress) {
		return lastTickPos + (destPos - lastTickPos) * progress;
	}

	public static AxisAlignedBB getLastTickBoundingBox(EntityLivingBase en) {
		final double minZ = en.lastTickPosZ - en.width * 0.5;
		final double maxZ = en.lastTickPosZ + en.width * 0.5;

		final double minX = en.lastTickPosX - en.width * 0.5;
		final double maxX = en.lastTickPosX + en.width * 0.5;

		final double minY = en.lastTickPosY;
		final double maxY = en.lastTickPosY + en.height;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static AxisAlignedBB getBoundingBox(double x, double y, double z, double width, double height) {
		final double minZ = z - width * 0.5;
		final double maxZ = z + width * 0.5;

		final double minX = x - width * 0.5;
		final double maxX = x + width * 0.5;

		final double maxY = y + height;

		return new AxisAlignedBB(minX, y, minZ, maxX, maxY, maxZ);
	}

	public static void drawBoundingBox(
			double x, double y, double z,
			double x2, double y2, double z2,
			double lastX, double lastY, double lastZ,
			double lastX2, double lastY2, double lastZ2,
			int r, int g, int b, int a, float progress
	) {
		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer worldRenderer = tess.getWorldRenderer();

		final double interpMinX = interpPos(x, lastX, progress);
		final double interpMinY = interpPos(y, lastY, progress);
		final double interpMinZ = interpPos(z, lastZ, progress);

		final double interpMaxX = interpPos(x2, lastX2, progress);
		final double interpMaxY = interpPos(y2, lastY2, progress);
		final double interpMaxZ = interpPos(z2, lastZ2, progress);

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

	public static void drawOutlinedBoundingBox(
			final AxisAlignedBB lastBoundingBox, final AxisAlignedBB boundingBox,
			final float lineWidth,
			final int r, final int g, final int b
	)
	{
		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer worldRenderer = tess.getWorldRenderer();

		final double minX1 = lastBoundingBox.minX;
		final double minX2 = boundingBox.minX;

		final double minY1 = lastBoundingBox.minY;
		final double minY2 = boundingBox.minY;

		final double minZ1 = lastBoundingBox.minZ;
		final double minZ2 = boundingBox.minZ;

		final double maxX1 = lastBoundingBox.maxX;
		final double maxX2 = boundingBox.maxX;

		final double maxY1 = lastBoundingBox.maxY;
		final double maxY2 = boundingBox.maxY;

		final double maxZ1 = lastBoundingBox.maxZ;
		final double maxZ2 = boundingBox.maxZ;

		final double interpMinX = interpPos(minX2, minX1, ServerRotation.timer.renderPartialTicks);
		final double interpMinY = interpPos(minY2, minY1, ServerRotation.timer.renderPartialTicks);
		final double interpMinZ = interpPos(minZ2, minZ1, ServerRotation.timer.renderPartialTicks);

		final double interpMaxX = interpPos(maxX2, maxX1, ServerRotation.timer.renderPartialTicks);
		final double interpMaxY = interpPos(maxY2, maxY1, ServerRotation.timer.renderPartialTicks);
		final double interpMaxZ = interpPos(maxZ2, maxZ1, ServerRotation.timer.renderPartialTicks);

		final double relMinX = interpMinX - renderManager.viewerPosX;
		final double relMaxX = interpMaxX - renderManager.viewerPosX;

		final double relMinY = interpMinY - renderManager.viewerPosY;
		final double relMaxY = interpMaxY - renderManager.viewerPosY;

		final double relMinZ = interpMinZ - renderManager.viewerPosZ;
		final double relMaxZ = interpMaxZ - renderManager.viewerPosZ;

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		GL11.glLineWidth(lineWidth);

		worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		worldRenderer.pos(relMinX, relMinY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMinY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMinY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMinY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMinY, relMinZ).color(r, g, b, 255).endVertex();

		tess.draw();

		worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		worldRenderer.pos(relMinX, relMaxY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMaxY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMaxY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMaxY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMaxY, relMinZ).color(r, g, b, 255).endVertex();

		tess.draw();

		worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		worldRenderer.pos(relMinX, relMinY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMaxY, relMinZ).color(r, g, b, 255).endVertex();

		worldRenderer.pos(relMaxX, relMinY, relMinZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMaxY, relMinZ).color(r, g, b, 255).endVertex();

		worldRenderer.pos(relMinX, relMinY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMinX, relMaxY, relMaxZ).color(r, g, b, 255).endVertex();

		worldRenderer.pos(relMaxX, relMinY, relMaxZ).color(r, g, b, 255).endVertex();
		worldRenderer.pos(relMaxX, relMaxY, relMaxZ).color(r, g, b, 255).endVertex();

		tess.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}
}
