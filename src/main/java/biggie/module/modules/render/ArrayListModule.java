package biggie.module.modules.render;

import biggie.event.render.Game2DEvent;
import biggie.manager.ModuleManager;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.IntegerSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ArrayListModule extends Module {
	public final IntegerSetting posX = new IntegerSetting(
			"Pos X",
			0,
			0,
			500,
			1
	);
	public final IntegerSetting posY = new IntegerSetting(
			"Pos Y",
			5,
			0,
			500,
			1
	);
	public final BooleanSetting background = new BooleanSetting("Background", true);

	public ArrayListModule() {
		super("ArrayList", ModuleCategory.RENDER, Keyboard.KEY_NONE);
	}

	@EventTarget(noParamEvents = Game2DEvent.class)
	public void onGame2D() {
		final ArrayList<Module> enabledModules = (ArrayList<Module>) ModuleManager.MODULES
				.parallelStream()
				.filter(Module::isEnabled)
				.collect(Collectors.toList());
		int offsetY = posY.value;

		for (Module module : enabledModules) {
			if (background.value) {
				int rectWidth = mc.fontRendererObj.getStringWidth(module.name) + 2;

				Gui.drawRect(
						posX.value,
						offsetY + 1,
						posX.value + rectWidth,
						offsetY + mc.fontRendererObj.FONT_HEIGHT + 2,
						new Color(0, 0, 0, 100).getRGB()
				);
			}

			mc.fontRendererObj.drawStringWithShadow(
					module.name,
					posX.value + 1,
					offsetY,
					-1
			);

			offsetY += mc.fontRendererObj.FONT_HEIGHT + 2;
		}
	}
}
