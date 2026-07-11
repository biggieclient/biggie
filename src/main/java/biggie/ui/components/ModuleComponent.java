package biggie.ui.components;

import biggie.module.Module;
import biggie.setting.Setting;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.setting.settings.ListSetting;
import biggie.ui.Component;
import biggie.ui.dataset.impl.DoubleSlider;
import biggie.ui.dataset.impl.IntSlider;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ModuleComponent implements Component {
	public Module mod;
	public CategoryComponent category;
	public int offsetY;
	public final ArrayList<Component> settings;
	public boolean panelExpand;

	public ModuleComponent(Module mod, CategoryComponent category, int offsetY) {
		this.mod = mod;
		this.category = category;
		this.offsetY = offsetY;
		this.settings = new ArrayList<>();
		this.panelExpand = false;
		int y = offsetY + 12;

		for (Setting<?> setting : mod.settings) {
			if (setting instanceof BooleanSetting) {
				BooleanSetting booleanSetting = (BooleanSetting) setting;
				CheckBoxComponent component = new CheckBoxComponent(booleanSetting, this, y);

				this.settings.add(component);

				y += component.getHeight();
			} else if (setting instanceof DoubleSetting) {
				DoubleSetting doubleSetting = (DoubleSetting) setting;
				SliderComponent component = new SliderComponent(new DoubleSlider(doubleSetting), this, y);

				this.settings.add(component);

				y += component.getHeight();
			} else if (setting instanceof IntegerSetting) {
				IntegerSetting integerSetting = (IntegerSetting) setting;
				SliderComponent component = new SliderComponent(new IntSlider(integerSetting), this, y);

				this.settings.add(component);

				y += component.getHeight();
			} else if (setting instanceof ListSetting) {
				ListSetting listSetting = (ListSetting) setting;
				ModeComponent component = new ModeComponent(listSetting, this, y);

				this.settings.add(component);

				y += component.getHeight();
			}
		}

		this.settings.add(new BindComponent(this, y));
	}

	public void setComponentStartAt(int newOffsetY) {
		this.offsetY = newOffsetY;
		int y = this.offsetY + 16;

		for (Component c : this.settings) {
			c.setComponentStartAt(y);

			if (c.isVisible()) {
				y += c.getHeight();
			}
		}
	}

	public void draw(AtomicInteger offset) {
		int textColor;

		if (this.mod.isEnabled()) {
			textColor = new Color(100, 0, 200).getRGB();
		} else {
			textColor = new Color(102, 102, 102).getRGB();
		}

		Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(this.mod.name, (float) (this.category.getX() + this.category.getWidth() / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.mod.name) / 2), (float) (this.category.getY() + this.offsetY + 4), textColor);

		if (this.panelExpand && !this.settings.isEmpty()) {
			for (Component c : this.settings) {
				if (c.isVisible()) {
					c.draw(offset);

					offset.incrementAndGet();
				}
			}
		}
	}

	public int getHeight() {
		if (!this.panelExpand) {
			return 16;
		} else {
			int h = 16;

			for (Component c : this.settings) {
				if (c.isVisible()) {
					h += c.getHeight();
				}
			}

			return h;
		}
	}

	public void update(int mousePosX, int mousePosY) {
		if (!panelExpand) return;

		if (!this.settings.isEmpty()) {
			for (Component c : this.settings) {
				if (c.isVisible()) {
					c.update(mousePosX, mousePosY);
				}
			}
		}
	}

	public void mouseDown(int x, int y, int button) {
		if (this.isHovered(x, y) && button == 0) {
			this.mod.toggle();
		}

		if (this.isHovered(x, y) && button == 1) {
			this.panelExpand = !this.panelExpand;
		}

		if (!panelExpand) return;

		for (Component c : this.settings) {
			if (c.isVisible()) {
				c.mouseDown(x, y, button);
			}
		}
	}

	public void mouseReleased(int x, int y, int button) {
		if (!panelExpand) return;

		for (Component c : this.settings) {
			if (c.isVisible()) {
				c.mouseReleased(x, y, button);
			}
		}
	}

	public void keyTyped(char chatTyped, int keyCode) {
		if (!panelExpand) return;

		for (Component c : this.settings) {
			if (c.isVisible()) {
				c.keyTyped(chatTyped, keyCode);
			}
		}
	}

	public boolean isHovered(int x, int y) {
		return x > this.category.getX() &&
				x < this.category.getX() + this.category.getWidth() &&
				y > this.category.getY() + this.offsetY &&
				y < this.category.getY() + 16 + this.offsetY;
	}

	@Override
	public boolean isVisible() {
		return true;
	}
}
