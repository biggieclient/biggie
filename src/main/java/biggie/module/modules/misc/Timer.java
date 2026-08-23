package biggie.module.modules.misc;

import biggie.event.motion.UpdateEvent;
import biggie.mixin.accessors.MinecraftAccessor;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import org.lwjgl.input.Keyboard;

public class Timer extends Module {
    private final DoubleSetting multiplier = new DoubleSetting("Multiplier", 1, 0, 2, 0.01);

    public Timer() {
        super("Timer", ModuleCategory.MISC, Keyboard.KEY_NONE);
    }

    @Override
    public String getInfo() {
        return multiplier.value.toString() + "x";
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != EnumEventType.PRE)
            return;

        ((MinecraftAccessor) mc).getTimer().timerSpeed = multiplier.value.floatValue();
    }

    @Override
    public void onDisable() {
        ((MinecraftAccessor) mc).getTimer().timerSpeed = 1.0f;
    }
}
