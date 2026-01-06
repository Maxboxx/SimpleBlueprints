package maxboxx.blueprints.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

public class TickUtil {
	public static TemporaryFreeze temporaryFreeze(LocalPlayer player) {
		boolean wasFrozen = isFrozen(player);

		if (!wasFrozen) {
			freeze(player);
		}

		return new TemporaryFreeze(wasFrozen, player);
	}

	public static void freeze(LocalPlayer player) {
		CommandUtil.sendCommand(player, "tick", "freeze");
	}

	public static void unfreeze(LocalPlayer player) {
		CommandUtil.sendCommand(player, "tick", "unfreeze");
	}

	public static void setFrozen(LocalPlayer player, boolean frozen) {
		if (frozen) {
			freeze(player);
		}
		else {
			unfreeze(player);
		}
	}

	public static boolean isFrozen(LocalPlayer player) {
		return isFrozen(player.level());
	}

	public static boolean isFrozen(Level level) {
		return level.tickRateManager().isFrozen();
	}

	public record TemporaryFreeze(boolean wasFrozen, LocalPlayer player) {
		public void unfreeze() {
			if (!wasFrozen) {
				TickUtil.unfreeze(player);
			}
		}
	}
}
