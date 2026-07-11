package biggie.ui.components;

import biggie.setting.settings.ListSetting;
import biggie.ui.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.atomic.AtomicInteger;

public class ModeComponent implements Component {
	private final ListSetting setting;
	private final ModuleComponent parentModule;
	private int x;
	private int y;
	private int offsetY;

	public ModeComponent(ListSetting setting, ModuleComponent parentModule, int offsetY) {
		this.setting = setting;
		this.parentModule = parentModule;
		this.x = parentModule.category.getX() + parentModule.category.getWidth();
		this.y = parentModule.category.getY() + parentModule.offsetY;
		this.offsetY = offsetY;
	}

	public void draw(AtomicInteger offset) {
		GL11.glPushMatrix();
		GL11.glScaled(0.5D, 0.5D, 0.5D);

		String mode = this.setting.value;
		mode = mode.replace("_", " ");

		int bruhWidth = (int) (Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.setting.name + ": ") * 0.5);
		String formattedValue = EnumChatFormatting.AQUA + mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase();

		Minecraft.getMinecraft().fontRendererObj.drawString(this.setting.name + ": ", (float) ((this.parentModule.category.getX() + 4) * 2), (float) ((this.parentModule.category.getY() + this.offsetY + 4) * 2), 0xffffffff, true);

		Minecraft.getMinecraft().fontRendererObj.drawString(formattedValue, (float) ((this.parentModule.category.getX() + 4 + bruhWidth) * 2), (float) ((this.parentModule.category.getY() + this.offsetY + 4) * 2), -1, true);

		GL11.glPopMatrix();
	}

	public void update(int mousePosX, int mousePosY) {
		this.y = this.parentModule.category.getY() + this.offsetY;
		this.x = this.parentModule.category.getX();
	}

	public void setComponentStartAt(int newOffsetY) {
		this.offsetY = newOffsetY;
	}

	@Override
	public int getHeight() {
		return 12;
	}


	public void mouseDown(int x, int y, int button) {
		if (isHovered(x, y)) {
			if (button == 0) {
				this.setting.nextMode();
			} else if (button == 1) {
				this.setting.previousMode();
			}
		}
	}

	@Override
	public void mouseReleased(int x, int y, int button) {

	}

	@Override
	public void keyTyped(char chatTyped, int keyCode) {

	}

	private boolean isHovered(int x, int y) {
		return x > this.x && x < this.x + this.parentModule.category.getWidth() && y > this.y && y < this.y + 11;
	}

	@Override
	public boolean isVisible() {
		return setting.showIf.get();
	}
}
