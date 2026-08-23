package biggie.module.modules.player;

import biggie.event.client.TickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.util.network.PacketUtil;
import biggie.util.player.InventoryUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

public class AutoSoup extends Module {
	private final DoubleSetting health = new DoubleSetting(
			"Health",
			16.0,
			1.0,
			20.0,
			0.5
	);

	private final IntegerSetting delay = new IntegerSetting(
			"Delay",
			10,
			0,
			500,
			1
	);

	private final BooleanSetting dropBowl = new BooleanSetting(
			"Drop Bowl",
			true
	);

	private boolean healed = false;
	private long lastMs = 0;

	public AutoSoup() {
		super("AutoSoup", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
	}

	@Override
	public String getInfo() {
		return "Blatant";
	}

	@Override
	public void onDisable() {
		healed = false;
		lastMs = 0;
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.theWorld == null || mc.thePlayer == null) {
			healed = false;
			return;
		}

		if (healed) {
			if (dropBowl.value) {
				PacketUtil.sendPacket(new C07PacketPlayerDigging(
						C07PacketPlayerDigging.Action.DROP_ITEM,
						BlockPos.ORIGIN,
						EnumFacing.DOWN
				));
			}

			PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

			healed = false;
			return;
		}

		final long currTime = System.currentTimeMillis();

		if (mc.thePlayer.getHealth() > health.value)
			return;

		if (!InventoryUtil.hasSoup(36, 45))
			return;

		if (currTime - lastMs < delay.value)
			return;

		final int soupSlot = InventoryUtil.getSoupInHotbar();

		PacketUtil.sendPacket(new C09PacketHeldItemChange(soupSlot - 36));
		PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(
				mc.thePlayer.inventoryContainer.getSlot(soupSlot).getStack()
		));

		healed = true;
		lastMs = currTime;
	}
}
