package maxboxx.blueprints;

import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.tools.BlueprintTools;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SimpleBlueprintsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyBinds.init();
		WorldRenderer.init();

		BlueprintTools.init();
		BlueprintManager.init();
	}
}
