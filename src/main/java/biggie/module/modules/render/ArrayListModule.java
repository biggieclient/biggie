package biggie.module.modules.render;

import biggie.event.render.RenderTickEvent;
import biggie.manager.ModuleManager;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

// TODO: Deixar ajustavel a posição com drag e adicionar outros modo de cor,
//  de preferencia colocar logo setting de cor invés de usar 2 rgb que ocupa muito espaço.
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

	public final ListSetting bars = new ListSetting(
			"Bars",
			"Left",
            "None", "Left", "Outline"
	);

	private final float invTicks = 1.0f / 100.0f;

	public ArrayListModule() {
		super("ArrayList", ModuleCategory.RENDER, Keyboard.KEY_NONE);
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRender2D() {
		if (mc.currentScreen != null)
			return;

		final ArrayList<Module> enabledModules = (ArrayList<Module>) ModuleManager.MODULES
				.parallelStream()
				.filter(Module::isEnabled)
				.sorted((e1, e2) -> mc.fontRendererObj.getStringWidth(e2.name + e2.getInfo()) - mc.fontRendererObj.getStringWidth(e1.name + e1.getInfo()))
				.collect(Collectors.toList());
		int offsetY = posY.value;

		final float progress = mc.thePlayer.ticksExisted * invTicks;
		final int size = enabledModules.size();

		for (int i = 0; i < size; ++i) {
			final Module module = enabledModules.get(i);
			final String text = module.name + (module.getInfo().isEmpty() ? "" : "§7 " + module.getInfo());

			final Color color = RenderUtil.getRGBColor(progress + (i * 2.0f * invTicks));

			if (background.value) {
				final int rectWidth = mc.fontRendererObj.getStringWidth(text) + 1;

				Gui.drawRect(
						posX.value,
						offsetY - ((i == 0) ? 1 : 0),
						posX.value + rectWidth,
						offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
						new Color(0, 0, 0, 100).getRGB()
				);
			}

			if (bars.value.equals("Left")) {
				Gui.drawRect(
						posX.value - 1,
						offsetY - (background.value ? 1 : 0),
						posX.value,
						offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
						color.getRGB()
				);
			} else if (bars.value.equals("Outline")) {
				final int rectWidth = mc.fontRendererObj.getStringWidth(text) + 1;
				final Module nextModule = (i == size - 1) ? null : enabledModules.get(i + 1);

				Gui.drawRect(
						posX.value - 1,
						offsetY,
						posX.value,
						offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
						color.getRGB()
				);

				if (i == 0) {
					Gui.drawRect(
							posX.value - 1,
							offsetY - 1,
							posX.value + rectWidth + 1,
							offsetY,
							color.getRGB()
					);
				}

				Gui.drawRect(
						posX.value + rectWidth,
						offsetY,
						posX.value + rectWidth + 1,
						offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
						color.getRGB()
				);

				if (i == size - 1) {
					Gui.drawRect(
							posX.value - 1,
							offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
							posX.value + rectWidth + 1,
							offsetY + mc.fontRendererObj.FONT_HEIGHT + 2,
							color.getRGB()
					);
				}

				if (nextModule != null) {
					final String nextText = nextModule.name + (nextModule.getInfo().isEmpty() ? "" : "§7 " + nextModule.getInfo());
					final int nextRectWidth = mc.fontRendererObj.getStringWidth(nextText) + 1;

					Gui.drawRect(
							posX.value + nextRectWidth + 1,
							offsetY + mc.fontRendererObj.FONT_HEIGHT + 1,
							posX.value + rectWidth + 1,
							offsetY + mc.fontRendererObj.FONT_HEIGHT + 2,
							color.getRGB()
					);
				}
			}

			mc.fontRendererObj.drawStringWithShadow(
					text,
					posX.value + 1,
					offsetY,
					color.getRGB()
			);

			offsetY += mc.fontRendererObj.FONT_HEIGHT + 1;
		}
	}
}
