package biggie.module.modules.misc;

import biggie.mixin.accessors.MinecraftAccessor;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import org.lwjgl.input.Keyboard;

public class Timer extends Module {
    private final DoubleSetting multiplier = new DoubleSetting("Multiplier", 1, 0, 1, 0.01);

    public Timer() {
        super("Timer", ModuleCategory.MISC, Keyboard.KEY_NONE);
    }

    @Override
    public String getInfo() {
        return multiplier.value.toString() + "x";
    }

    @Override
    public void onEnable() {
        ((MinecraftAccessor) mc).getTimer().timerSpeed = multiplier.value.floatValue();
    }

    @Override
    public void onDisable() {
        ((MinecraftAccessor) mc).getTimer().timerSpeed = 1.0f;
    }
}
