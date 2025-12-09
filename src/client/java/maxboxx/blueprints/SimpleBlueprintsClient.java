package maxboxx.blueprints;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class SimpleBlueprintsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyBinds.init();
		BlueprintManager.init();
	}
}
