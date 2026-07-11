package biggie.ui;

import biggie.event.render.ClickGuiEvent;
import biggie.module.ModuleCategory;
import biggie.ui.components.BindComponent;
import biggie.ui.components.CategoryComponent;
import biggie.ui.components.ModuleComponent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;

// from openmyau
public class ClickGui extends GuiScreen {
	private static ClickGui instance;
	private final ArrayList<CategoryComponent> categoryList;

	public ClickGui() {
		this.categoryList = new ArrayList<>();
		int topOffset = 5;

		instance = this;

		for (ModuleCategory moduleCategory : ModuleCategory.values()) {
			CategoryComponent misc = new CategoryComponent(moduleCategory);

			misc.setY(topOffset);

			categoryList.add(misc);

			topOffset += 20;
		}
	}

	public static ClickGui getInstance() {
		return instance;
	}

	public void initGui() {
		super.initGui();
	}

	public void drawScreen(int x, int y, float p) {
		EventManager.call(new ClickGuiEvent());

		drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 100).getRGB());

		mc.fontRendererObj.drawStringWithShadow("Byteware", 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT * 2, new Color(60, 162, 253).getRGB());
		mc.fontRendererObj.drawStringWithShadow("dev: dev", 4, this.height - 3 - mc.fontRendererObj.FONT_HEIGHT, new Color(60, 162, 253).getRGB());

		for (CategoryComponent category : categoryList) {
			category.render(this.fontRendererObj);

			category.handleDrag(x, y);

			for (Component module : category.getModules()) {
				module.update(x, y);
			}
		}

		int wheel = Mouse.getDWheel();

		if (wheel != 0) {
			int scrollDir = wheel > 0 ? 1 : -1;

			for (CategoryComponent category : categoryList) {
				category.onScroll(x, y, scrollDir);
			}
		}
	}

	public void mouseClicked(int x, int y, int mouseButton) {
		for (CategoryComponent categoryComponent : categoryList) {
			if (categoryComponent.insideArea(x, y) &&
					!categoryComponent.isHovered(x, y) &&
					!categoryComponent.mousePressed(x, y) &&
					mouseButton == 0
			) {
				categoryComponent.mousePressed(true);

				categoryComponent.xx = x - categoryComponent.getX();
				categoryComponent.yy = y - categoryComponent.getY();
			}

			if (categoryComponent.mousePressed(x, y) && mouseButton == 0) {
				categoryComponent.setOpened(!categoryComponent.isOpened());

				if (!categoryComponent.isOpened()) {
					for (ModuleComponent moduleComponent : categoryComponent.getModules()) {
						for (Component component : moduleComponent.settings) {
							if (component instanceof BindComponent) {
								BindComponent bindComponent = (BindComponent) component;

								if (bindComponent.isBinding) {
									bindComponent.isBinding = false;
								}
							}
						}
					}
				}
			}

			if (categoryComponent.isHovered(x, y) && mouseButton == 0) {
				categoryComponent.setPin(!categoryComponent.isPin());
			}

			if (categoryComponent.isOpened()) {
				for (Component component : categoryComponent.getModules()) {
					component.mouseDown(x, y, mouseButton);
				}
			}
		}
	}

	public void mouseReleased(int x, int y, int mouseButton) {
		for (CategoryComponent categoryComponent : categoryList) {
			if (mouseButton == 0) {
				categoryComponent.mousePressed(false);
			}

			if (categoryComponent.isOpened()) {
				for (Component component : categoryComponent.getModules()) {
					component.mouseReleased(x, y, mouseButton);
				}
			}
		}
	}

	public void keyTyped(char typedChar, int key) {
		if (key == Keyboard.KEY_ESCAPE) {
			for (CategoryComponent categoryComponent : categoryList) {
				for (ModuleComponent moduleComponent : categoryComponent.getModules()) {
					for (Component component : moduleComponent.settings) {
						if (component instanceof BindComponent) {
							BindComponent bindComponent = (BindComponent) component;

							if (bindComponent.isBinding) {
								bindComponent.isBinding = false;

								return;
							}
						}
					}
				}
			}

			this.mc.displayGuiScreen(null);
		} else {
			for (CategoryComponent category : categoryList) {
				if (category.isOpened()) {
					for (Component component : category.getModules()) {
						component.keyTyped(typedChar, key);
					}
				}
			}
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
