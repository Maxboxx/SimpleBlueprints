package maxboxx.blueprints.utils;

import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.network.chat.Component;

public class Txt {
	public static Component key(String key) {
		return Component.translatable(SimpleBlueprints.MOD_ID + "." + key);
	}

	public static Component key(String key, Object... objects) {
		return Component.translatable(SimpleBlueprints.MOD_ID + "." + key, objects);
	}
}
