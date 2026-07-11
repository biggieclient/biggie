package biggie.module.modules.render;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.ui.ClickGui;
import org.lwjgl.input.Keyboard;

public class ClickGuiModule extends Module {
	private ClickGui clickGui;

	public ClickGuiModule() {
		super("ClickGUI", ModuleCategory.RENDER, Keyboard.KEY_RSHIFT);
	}

	@Override
	public void onEnable() {
		if (clickGui == null) {
			clickGui = new ClickGui();
		}

		mc.displayGuiScreen(clickGui);

		setEnabled(false);
	}
}
