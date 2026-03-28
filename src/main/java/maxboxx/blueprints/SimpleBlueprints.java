package maxboxx.blueprints;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class SimpleBlueprints implements ModInitializer {
	public static final String MOD_ID = "simple_blueprints";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Simple Blueprints Initialized");
	}

	public static Path configPath() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
		File dir = path.toFile();

		if (!dir.exists()) {
			try {
				dir.mkdirs();
			}
			catch (Exception e) {
				LOGGER.error("Failed to create config dir", e);
			}
		}

		return path;
	}

	public static Path configFilePath(String fileName) {
		return configPath().resolve(fileName);
	}
}