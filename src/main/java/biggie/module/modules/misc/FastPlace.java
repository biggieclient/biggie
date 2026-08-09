package biggie.module.modules.misc;

import biggie.event.client.TickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", ModuleCategory.MISC, Keyboard.KEY_NONE);
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() != EnumEventType.PRE)
            return;

        ReflectionHelper.setPrivateValue(Minecraft.class, mc, 0, "field_71467_ac", "rightClickDelayTimer");
    }
}
