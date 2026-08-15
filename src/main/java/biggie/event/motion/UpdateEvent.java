package biggie.event.motion;

import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.lenni0451.asmevents.event.wrapper.TypedEvent;

public class UpdateEvent extends TypedEvent {
	public UpdateEvent(EnumEventType type) {
		super(type);
	}
}
