package maxboxx.blueprints;

import maxboxx.blueprints.data.Settings;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.tools.BlueprintTools;
import net.fabricmc.api.ClientModInitializer;

public class SimpleBlueprintsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Settings.load();
		KeyBinds.init();
		WorldRenderer.init();

		BlueprintTools.init();
		BlueprintManager.init();
	}
}
