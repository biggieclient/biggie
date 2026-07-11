package biggie.event.motion;

import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.lenni0451.asmevents.event.wrapper.TypedEvent;

public class LivingUpdateEvent extends TypedEvent {
	public LivingUpdateEvent(EnumEventType type) {
		super(type);
	}
}
