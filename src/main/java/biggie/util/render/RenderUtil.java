package biggie.util.render;

import biggie.util.player.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil {
	public static double interpPos(final double destPos, final double lastTickPos, final float progress) {
		return lastTickPos + (destPos - lastTickPos) * progress;
	}

	public static AxisAlignedBB getLastTickBoundingBox(final EntityLivingBase en) {
		final double minZ = en.lastTickPosZ - en.width * 0.5;
		final double maxZ = en.lastTickPosZ + en.width * 0.5;

		final double minX = en.lastTickPosX - en.width * 0.5;
		final double maxX = en.lastTickPosX + en.width * 0.5;

		final double minY = en.lastTickPosY;
		final double maxY = en.lastTickPosY + en.height;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public static AxisAlignedBB getBoundingBox(final double x, final double y, final double z, final double width, final double height) {
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
			final AxisAlignedBB lastBoundingBox, final AxisAlignedBB boundingBox,
			final int r, final int g, final int b, final int a
	) {
		drawBoundingBox(
				boundingBox.minX, boundingBox.minY, boundingBox.minZ,
				boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ,
				lastBoundingBox.minX, lastBoundingBox.minY, lastBoundingBox.minZ,
				lastBoundingBox.maxX, lastBoundingBox.maxY, lastBoundingBox.maxZ,
				r, g, b, a,
				ServerRotation.timer.renderPartialTicks
		);
	}

	public static void drawBoundingBox(
			final AxisAlignedBB lastBoundingBox, final AxisAlignedBB boundingBox,
			final int r, final int g, final int b, final int a, final float progress
	) {
		drawBoundingBox(
				boundingBox.minX, boundingBox.minY, boundingBox.minZ,
				boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ,
				lastBoundingBox.minX, lastBoundingBox.minY, lastBoundingBox.minZ,
				lastBoundingBox.maxX, lastBoundingBox.maxY, lastBoundingBox.maxZ,
				r, g, b, a,
				progress
		);
	}

	public static void drawBoundingBox(
			final double x, final double y, final double z,
			final double x2, final double y2, final double z2,
			final double lastX, final double lastY, final double lastZ,
			final double lastX2, final double lastY2, final double lastZ2,
			final int r, final int g, final int b, final int a
	) {
		drawBoundingBox(
				x, y, z,
				x2, y2, z2,
				lastX, lastY, lastZ,
				lastX2, lastY2, lastZ2,
				r, g, b, a,
				ServerRotation.timer.renderPartialTicks
		);
	}

	public static void drawOutlinedBoundingBox(
			final AxisAlignedBB lastBoundingBox, final AxisAlignedBB boundingBox,
			final float lineWidth,
			final int r, final int g, final int b, final float progress
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

		final double interpMinX = interpPos(minX2, minX1, progress);
		final double interpMinY = interpPos(minY2, minY1, progress);
		final double interpMinZ = interpPos(minZ2, minZ1, progress);

		final double interpMaxX = interpPos(maxX2, maxX1, progress);
		final double interpMaxY = interpPos(maxY2, maxY1, progress);
		final double interpMaxZ = interpPos(maxZ2, maxZ1, progress);

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

	public static void drawWorldRect(
			final Vec3 lastPos, final Vec3 pos, final double width, final double height,
			final int r, final int g, final int b, final int a, final float progress
	) {
		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
		final Tessellator tess = Tessellator.getInstance();
		final WorldRenderer worldRenderer = tess.getWorldRenderer();

		final double halfWidth = width * 0.5f;

		final double minX1 = lastPos.xCoord - halfWidth;
		final double maxX1 = lastPos.xCoord + halfWidth;

		final double minX2 = pos.xCoord - halfWidth;
		final double maxX2 = pos.xCoord + halfWidth;

		final double minY1 = lastPos.yCoord;
		final double maxY1 = lastPos.yCoord + height;

		final double minY2 = pos.yCoord;
		final double maxY2 = pos.yCoord + height;

		final double interpMinX = interpPos(minX2, minX1, progress);
		final double interpMinY = interpPos(minY2, minY1, progress);

		final double interpMaxX = interpPos(maxX2, maxX1, progress);
		final double interpMaxY = interpPos(maxY2, maxY1, progress);

		final double interpZ = interpPos(pos.zCoord, lastPos.zCoord, progress);

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		worldRenderer.pos(interpMinX, interpMinY, interpZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(interpMinX, interpMaxY, interpZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(interpMaxX, interpMaxY, interpZ).color(r, g, b, a).endVertex();
		worldRenderer.pos(interpMaxX, interpMinY, interpZ).color(r, g, b, a).endVertex();

		tess.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPopMatrix();
	}

	public static void drawWorldText(
			final Vec3 lastPos, final Vec3 pos, final String text,
			final int r, final int g, final int b, final int a, final float scale, final float progress
	) {
		final double width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
		final double height = Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;

		final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

		final double interpX = interpPos(pos.xCoord, lastPos.xCoord, progress);
		final double interpY = interpPos(pos.yCoord, lastPos.yCoord, progress);
		final double interpZ = interpPos(pos.zCoord, lastPos.zCoord, progress);

		final double relX = interpX - renderManager.viewerPosX;
		final double relY = interpY - renderManager.viewerPosY;
		final double relZ = interpZ - renderManager.viewerPosZ;

		GL11.glPushMatrix();

		GL11.glTranslated(relX, relY, relZ);

		GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0f, 0.0F);
		GL11.glRotatef(renderManager.playerViewX, 1.0f, 0.0F, 0.0F);

		GL11.glScaled(-scale, -scale, scale);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_CULL_FACE);

		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, (float) (-width * 0.5), (float) -height, new Color(r, g, b, a).getRGB());

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);

		GL11.glPopMatrix();
	}

	public static void drawWorldText(
			final Vec3 lastPos, final Vec3 pos, final String text,
			final int r, final int g, final int b, final int a, final float scale
	) {
		drawWorldText(lastPos, pos, text, r, g, b, a, scale, ServerRotation.timer.renderPartialTicks);
	}

	public static void drawWorldRect(
			final Vec3 lastPos, final Vec3 pos, final double width, final double height,
			final int r, final int g, final int b, final int a
	) {
		drawWorldRect(lastPos, pos, width, height, r, g, b, a, ServerRotation.timer.renderPartialTicks);
	}

	public static void drawOutlinedBoundingBox(
			final AxisAlignedBB lastBoundingBox, final AxisAlignedBB boundingBox,
			final float lineWidth,
			final int r, final int g, final int b
	) {
		drawOutlinedBoundingBox(lastBoundingBox, boundingBox, lineWidth, r, g, b, ServerRotation.timer.renderPartialTicks);
	}

	public static Color getRGBColor(final float progress) {
		return Color.getHSBColor(progress, 1.0f, 1.0f);
	}

	public static Color getInterpolatedColor(final Color color1, final Color color2, final float progress) {
		final int dR = color2.getRed() - color1.getRed();
		final int dG = color2.getGreen() - color1.getGreen();
		final int dB = color2.getBlue() - color1.getBlue();

		final int r = color1.getRed() + (int) (dR * progress);
		final int g = color1.getGreen() + (int) (dG * progress);
		final int b = color1.getBlue() + (int) (dB * progress);

		return new Color(r, g, b);
	}

	public static Color getInterpolatedColor(final Color color1, final Color color2, final Color color3, final float progress) {
		final int dR1 = color2.getRed() - color1.getRed();
		final int dG1 = color2.getGreen() - color1.getGreen();
		final int dB1 = color2.getBlue() - color1.getBlue();

		final int r1 = color1.getRed() + (int) (dR1 * progress);
		final int g1 = color1.getGreen() + (int) (dG1 * progress);
		final int b1 = color1.getBlue() + (int) (dB1 * progress);

		final int dR2 = color3.getRed() - color2.getRed();
		final int dG2 = color3.getGreen() - color2.getGreen();
		final int dB2 = color3.getBlue() - color2.getBlue();

		final int r2 = color2.getRed() + (int) (dR2 * progress);
		final int g2 = color2.getGreen() + (int) (dG2 * progress);
		final int b2 = color2.getBlue() + (int) (dB2 * progress);

		final int dR = r2 - r1;
		final int dG = g2 - g1;
		final int dB = b2 - b1;

		final int r = r1 + (int) (dR * progress);
		final int g = g1 + (int) (dG * progress);
		final int b = b1 + (int) (dB * progress);

		return new Color(r, g, b);
	}
}
