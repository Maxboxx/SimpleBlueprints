package maxboxx.blueprints;

import net.fabricmc.api.ModInitializer;

import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleBlueprints implements ModInitializer {
	public static final String MOD_ID = "simple_blueprints";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Blueprints Loaded");
	}

	public static Component text(String key) {
		return Component.translatable(MOD_ID + "." + key);
	}

	public static Component text(String key, Object... objects) {
		return Component.translatable(MOD_ID + "." + key, objects);
	}
}