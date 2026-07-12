package biggie.mixin.accessors;

import net.minecraftforge.client.event.MouseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseEvent.class)
public interface MouseEventAccessor {
	@Accessor(value = "button", remap = false)
	void setButton(int button);

	@Accessor(value = "buttonstate", remap = false)
	void setButtonState(boolean buttonState);
}
