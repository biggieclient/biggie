package biggie.event.motion;

import net.lenni0451.asmevents.event.IEvent;

public class ItemSlowDownEvent implements IEvent {
	public float forward, strafe;
	public boolean sprint;

	public ItemSlowDownEvent(
			float forward,
			float strafe,
			boolean sprint
	) {
		this.forward = forward;
		this.strafe = strafe;
		this.sprint = sprint;
	}
}
