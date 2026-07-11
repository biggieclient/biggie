package biggie.util.misc;

import biggie.mixin.accessors.MouseEventAccessor;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;

public class MouseUtil {
	public static void setButtonState(int button, boolean state) {
		MouseEvent mouseEvent = new MouseEvent();

		((MouseEventAccessor) mouseEvent).setButton(button);
		((MouseEventAccessor) mouseEvent).setButtonState(state);

		MinecraftForge.EVENT_BUS.post(mouseEvent);
	}
}
