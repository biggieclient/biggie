package biggie.mixin.accessors;

import net.minecraftforge.client.event.MouseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseEvent.class)
public interface MouseEventAccessor {
	@Accessor("button")
	void setButton(int button);

	@Accessor("buttonstate")
	void setButtonState(boolean buttonState);
}
