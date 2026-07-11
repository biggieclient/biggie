package biggie.event.client;

import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.lenni0451.asmevents.event.wrapper.TypedEvent;
import net.minecraft.entity.Entity;

public class AttackEvent extends TypedEvent {
	public final Entity entity;

	public AttackEvent(EnumEventType type, Entity entity) {
		super(type);
		this.entity = entity;
	}
}
