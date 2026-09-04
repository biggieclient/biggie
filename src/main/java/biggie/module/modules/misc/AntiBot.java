package biggie.module.modules.misc;

import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AntiBot extends Module {
	public static List<EntityPlayer> botList = new ArrayList<>();
	private long lastCheckMs = 0;

	private final DoubleSetting reCheckDelay = new DoubleSetting("Re-Check Delay", 250, 50, 1000, 50);

	public AntiBot() {
		super("AntiBot", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		botList.clear();
		lastCheckMs = 0;
	}

	@EventTarget(noParamEvents = LoadWorldEvent.class)
	public void onLoadWorld() {
		botList.clear();
		lastCheckMs = 0;
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		final long currTime = System.currentTimeMillis();

		if (currTime - lastCheckMs < reCheckDelay.value)
			return;

		for (final EntityPlayer enPlayer : mc.theWorld.playerEntities) {
			if (botList.contains(enPlayer))
				continue;

			if (enPlayer.isDead)
				continue;

			if (enPlayer == mc.thePlayer)
				continue;

			final UUID playerUUID = enPlayer.getUniqueID();

			if (playerUUID == null || mc.getNetHandler().getPlayerInfo(playerUUID) == null) {
				addBot(enPlayer, currTime);

				continue;
			}

			if (enPlayer.getHealth() <= 0.0f && enPlayer.getMaxHealth() <= 0.0f)
				addBot(enPlayer, currTime);
		}
	}

	private void addBot(final EntityPlayer enPlayer, final long currTime) {
		botList.add(enPlayer);
		lastCheckMs = currTime;
	}
}
