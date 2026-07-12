package biggie.addons;

import net.minecraft.network.Packet;

public interface NetworkManagerAddon {
	void biggie$sendPacketNoEvent(Packet<?> packet);
}
