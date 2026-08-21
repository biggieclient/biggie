package biggie.event.input;

import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.lenni0451.asmevents.event.wrapper.TypedEvent;

public class PlayerInputEvent extends TypedEvent {
    public float forward;
    public float strafe;

    public PlayerInputEvent(EnumEventType type, float forward, float strafe) {
		super(type);

		this.forward = forward;
        this.strafe = strafe;
    }
}
