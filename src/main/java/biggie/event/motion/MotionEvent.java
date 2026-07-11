package biggie.event.motion;

import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.lenni0451.asmevents.event.wrapper.TypedEvent;

public class MotionEvent extends TypedEvent {
	public double x, y, z;
	public float yaw, pitch;
	public boolean onGround;

	public MotionEvent(
			EnumEventType type,
			double x,
			double y,
			double z,
			float yaw,
			float pitch,
			boolean onGround
	) {
		super(type);

		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.onGround = onGround;
	}
}
