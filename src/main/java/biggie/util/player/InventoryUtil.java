package biggie.util.player;

import biggie.util.AbstractUtil;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;

public class InventoryUtil extends AbstractUtil {

	public static int getSoupInHotbar() {
		for (int i = 36; i < 45; i++) {
			ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

			if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
				return i;
			}
		}

		return -1;
	}

	public static boolean hasSoup(int from, int to) {
		for (int i = from; i < to; i++) {
			ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

			if (itemStack != null && itemStack.getItem() instanceof ItemSoup) {
				return true;
			}
		}

		return false;
	}
}
