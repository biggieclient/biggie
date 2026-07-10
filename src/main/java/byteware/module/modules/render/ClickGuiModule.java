package byteware.module.modules.render;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.ui.ClickGui;
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
