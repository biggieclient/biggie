package biggie.event.render;

import net.lenni0451.asmevents.event.wrapper.CancellableEvent;
import net.minecraft.entity.Entity;

public class RenderEntityNameTagEvent extends CancellableEvent {
    public final Entity en;

    public RenderEntityNameTagEvent(final Entity en) {
        this.en = en;
    }
}
