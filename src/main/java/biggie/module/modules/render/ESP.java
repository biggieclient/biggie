package biggie.module.modules.render;

import biggie.event.render.Render3DEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import net.lenni0451.asmevents.event.EventTarget;
import org.lwjgl.input.Keyboard;

public class ESP extends Module {
    private final BooleanSetting lineBox = new BooleanSetting("Line Box", true);

    public ESP() {
        super("ESP", ModuleCategory.RENDER, Keyboard.KEY_NONE);
    }

    @EventTarget(noParamEvents = Render3DEvent.class)
    public void onRender3D() {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (lineBox.value) {}
    }
}
