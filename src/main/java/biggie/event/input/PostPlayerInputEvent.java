package biggie.event.input;

import net.lenni0451.asmevents.event.IEvent;

public class PostPlayerInputEvent implements IEvent {
    public float moveForward;
    public float moveStrafe;

    public PostPlayerInputEvent(float moveForward, float moveStrafe) {
        this.moveForward = moveForward;
        this.moveStrafe = moveStrafe;
    }
}
