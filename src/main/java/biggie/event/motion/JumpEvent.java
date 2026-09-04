package biggie.event.motion;

import net.lenni0451.asmevents.event.IEvent;

public class JumpEvent implements IEvent {
	public float yaw;

	public JumpEvent(float yaw) {
		this.yaw = yaw;
	}
}
