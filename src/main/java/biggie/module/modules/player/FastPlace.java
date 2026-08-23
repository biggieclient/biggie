package biggie.module.modules.player;

import biggie.event.client.TickEvent;
import biggie.mixin.accessors.MinecraftAccessor;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.IntegerSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.item.ItemBlock;
import org.lwjgl.input.Keyboard;

public class FastPlace extends Module {
	private final IntegerSetting delay = new IntegerSetting(
			"Delay",
			1,
			0,
			5,
			1
	);

    private final BooleanSetting blockCheck = new BooleanSetting(
            "Block Check",
            true
    );

    public FastPlace() {
        super("FastPlace", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
    }

    @Override
    public String getInfo() {
        return delay.value.toString();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (event.getType() != EnumEventType.PRE)
            return;

        if (blockCheck.value && (mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock)))
            return;

		((MinecraftAccessor) mc).setRightClickDelayTimer(delay.value);
    }
}
