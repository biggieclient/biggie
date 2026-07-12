package biggie.event.motion;

import net.lenni0451.asmevents.event.wrapper.CancellableEvent;

public class StrafeEvent extends CancellableEvent {
	public float yaw, strafe, forward, friction;

	public StrafeEvent(float yaw, float strafe, float forward, float friction) {
		this.yaw = yaw;
		this.strafe = strafe;
		this.forward = forward;
		this.friction = friction;
	}
}
