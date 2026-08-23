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
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

// TODO: Deixar ajustavel a posição com drag.
public class ArrayListModule extends Module {
	private final BooleanSetting alignRight = new BooleanSetting(
			"Align Right",
			true
	);

	private final IntegerSetting offsetX = new IntegerSetting(
			"Offset X",
			0,
			0,
			500,
			1
	);
	private final IntegerSetting offsetY = new IntegerSetting(
			"Offset Y",
			5,
			0,
			500,
			1
	);

	private final BooleanSetting background = new BooleanSetting(
			"Background",
			true
	);

	private final BooleanSetting lowerCase = new BooleanSetting(
			"Lower Case",
			true
	);

	private final ListSetting bars = new ListSetting(
			"Bars",
			"Side",
            "None",
			"Side",
			"Outline"
	);

	public static final ListSetting COLOR = new ListSetting(
			"Color",
			"Blue",
			"Red",
			"Blue",
			"Aqua"
	);

	public static final float INV_TICKS = 1.0f / 100.0f;

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
				.sorted((e1, e2) -> {
					final String e2Text;
					final String e1Text;

					if (lowerCase.value) {
						e2Text = e2.name + (e2.getInfo().isEmpty() ? "" : "§7 " + e2.getInfo());
						e1Text = e1.name + (e1.getInfo().isEmpty() ? "" : "§7 " + e1.getInfo());
					} else {
						e2Text = e2.name + (e2.getInfo().isEmpty() ? "" : "§7 " + e2.getInfo()).toLowerCase();
						e1Text = e1.name + (e1.getInfo().isEmpty() ? "" : "§7 " + e1.getInfo()).toLowerCase();
					}

					return mc.fontRendererObj.getStringWidth(e2Text) - mc.fontRendererObj.getStringWidth(e1Text);
				})
				.collect(Collectors.toList());

		int modOffsetY = offsetY.value;

		final float progress = mc.thePlayer.ticksExisted * INV_TICKS;

		final int size = enabledModules.size();

		final Color[] colors = getColors(COLOR.value);
		final ScaledResolution scaledRes = new ScaledResolution(mc);

		for (int i = 0; i < size; ++i) {
			final Module module = enabledModules.get(i);
			String text = module.name + (module.getInfo().isEmpty() ? "" : "§7 " + module.getInfo());

			if (lowerCase.value)
				text = text.toLowerCase();

			float modProgress = (progress + (10 * INV_TICKS * i));
			modProgress -= (int) modProgress;

			final Color color = RenderUtil.getInterpolatedColor(colors[0], colors[1], colors[0], modProgress);

			final int textWidth = mc.fontRendererObj.getStringWidth(text);

			final int leftX = alignRight.value ? scaledRes.getScaledWidth() - textWidth - offsetX.value : offsetX.value;

			final boolean outline = bars.value.equals("Outline");
			final boolean basicBar = bars.value.equals("Side") || outline;

			final int backgroundLeftX = leftX - (!alignRight.value && basicBar ? 1 : (outline ? 1 : 0));
			final int backgroundRightX = leftX + textWidth + (alignRight.value && basicBar ? 1 : (outline ? 1 : 0));

			final int barLeftX = alignRight.value ? backgroundRightX : backgroundLeftX - 1;
			final int barRightX = barLeftX + 1;

			final int topBarLeftX = backgroundLeftX - 1;
			final int topBarRightX = backgroundRightX + 1;

			final int otherBarLeftX = alignRight.value ? backgroundLeftX - 1 : backgroundRightX;
			final int otherBarRightX = otherBarLeftX + 1;

			final boolean expandTop = bars.value.equals("Outline") && i == 0;

			final int backgroundTop = modOffsetY - (expandTop ? 1 : 0);
			final int backgroundBottom = modOffsetY + mc.fontRendererObj.FONT_HEIGHT + 1;

			if (background.value) {
				Gui.drawRect(
						backgroundLeftX,
						backgroundTop,
						backgroundRightX,
						backgroundBottom,
						new Color(0, 0, 0, 125).getRGB()
				);
			}

			if (basicBar) {
				Gui.drawRect(
						barLeftX,
						backgroundTop,
						barRightX,
						backgroundBottom,
						color.getRGB()
				);
			}

			if (bars.value.equals("Outline")) {
				if (expandTop) {
					Gui.drawRect(
							topBarLeftX,
							backgroundTop - 1,
							topBarRightX,
							backgroundTop,
							color.getRGB()
					);
				}

				if (i + 1 == enabledModules.size()) {
					Gui.drawRect(
							topBarLeftX,
							backgroundBottom,
							topBarRightX,
							backgroundBottom + 1,
							color.getRGB()
					);
				} else {
					final Module nextModule = enabledModules.get(i + 1);
					String nextText = nextModule.name + (nextModule.getInfo().isEmpty() ? "" : "§7 " + nextModule.getInfo());

					if (lowerCase.value)
						nextText = nextText.toLowerCase();

					final int nextTextWidth = mc.fontRendererObj.getStringWidth(nextText);

					final int nextLeftX = alignRight.value ? scaledRes.getScaledWidth() - nextTextWidth - offsetX.value : offsetX.value;

					final int nextBackLeftX = nextLeftX - (!alignRight.value && basicBar ? 1 : (outline ? 1 : 0));
					final int nextBackRightX = nextLeftX + nextTextWidth + 1;

					final int bottomBarLeftX = alignRight.value ? backgroundLeftX - 1 : nextBackRightX;
					final int bottomBarRightX = alignRight.value ? nextBackLeftX - 1 : backgroundRightX + 1;

					Gui.drawRect(
							bottomBarLeftX,
							backgroundBottom,
							bottomBarRightX,
							backgroundBottom + 1,
							color.getRGB()
					);
				}

				Gui.drawRect(
						otherBarLeftX,
						backgroundTop,
						otherBarRightX,
						backgroundBottom,
						color.getRGB()
				);
			}

			mc.fontRendererObj.drawStringWithShadow(
					text,
					leftX,
					modOffsetY,
					color.getRGB()
			);

			modOffsetY += mc.fontRendererObj.FONT_HEIGHT + 1;
		}
	}

	public static Color[] getColors(final String color) {
		final Color[] colors = new Color[] { Color.WHITE, Color.WHITE };

		switch (color) {
			case "Red":
				colors[0] = new Color(211, 80, 112);
				colors[1] = new Color(220, 172, 192);
				break;

			case "Blue":
				colors[0] = new Color(95, 128, 211);
				colors[1] = new Color(161, 204, 241);
				break;

			case "Aqua":
				colors[0] = new Color(45, 144, 214);
				colors[1] = new Color(116, 173, 198);
				break;
		}

		return colors;
	}
}
