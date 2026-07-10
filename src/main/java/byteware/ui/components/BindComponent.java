package byteware.ui.components;

import byteware.ui.Component;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BindComponent implements Component {
	public boolean isBinding;
	private final ModuleComponent parentModule;
	private int offsetY;
	private int x;
	private int y;

	public BindComponent(ModuleComponent b, int offsetY) {
		this.parentModule = b;
		this.x = b.category.getX() + b.category.getWidth();
		this.y = b.category.getY() + b.offsetY;
		this.offsetY = offsetY;
	}

	public void draw(AtomicInteger offset) {
		GL11.glPushMatrix();
		GL11.glScaled(0.5D, 0.5D, 0.5D);

		String displayText = isBinding ? "Press a key..." : "Bind" + ": " + Keyboard.getKeyName(parentModule.mod.keybind).toLowerCase();

		renderText(displayText, new Color(100, 0, 200).getRGB());

		GL11.glPopMatrix();
	}

	@Override
	public void update(int mousePosX, int mousePosY) {
		y = parentModule.category.getY() + offsetY;
		x = parentModule.category.getX();
	}

	public void mouseDown(int x, int y, int button) {
		if (isHovered(x, y) && button == 0 && parentModule.panelExpand) {
			isBinding = !isBinding;
		} else if (isBinding && parentModule.panelExpand) {
			int keyIndex = button - 100;

			if (button == 0) {
				isBinding = false;
				return;
			}

			parentModule.mod.keybind = keyIndex;
			isBinding = false;
		}
	}

	@Override
	public void mouseReleased(int x, int y, int button) {

	}

	@Override
	public void keyTyped(char chatTyped, int keyCode) {
		if (isBinding) {
			if (keyCode == Keyboard.KEY_ESCAPE) {
				isBinding = false;
				return;
			}

			if (keyCode == Keyboard.KEY_BACK) {
				parentModule.mod.keybind = 0;
			} else {
				parentModule.mod.keybind = keyCode;
			}

			isBinding = false;
		}
	}

	@Override
	public void setComponentStartAt(int newOffsetY) {
		offsetY = newOffsetY;
	}

	public boolean isHovered(int x, int y) {
		return x > this.x &&
				x < this.x + parentModule.category.getWidth() &&
				y > this.y - 1 &&
				y < this.y + 12;
	}

	public int getHeight() {
		return 12;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	private void renderText(String s, int color) {
		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
				s,
				(float) ((parentModule.category.getX() + 4) * 2),
				(float) ((parentModule.category.getY() + offsetY + 3) * 2),
				color
		);
	}
}
