package maxboxx.blueprints;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;

public class BlueprintManager {
	private static boolean isActive = false;

	public static void init() {
		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (isActive) {
				boolean leftClick  = KeyBinds.consume(client.options.keyAttack);
				boolean rightClick = KeyBinds.consume(client.options.keyUse);

				if (leftClick) {
					SimpleBlueprints.LOGGER.info("Left Click Consumed");
				}

				if (rightClick) {
					SimpleBlueprints.LOGGER.info("Right Click Consumed");
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (KeyBinds.TOGGLE.consumeClick()) {
				toggleState();
			}
		});
	}

	private static void toggleState() {
		isActive = !isActive;

		if (isActive) {
			SimpleBlueprints.LOGGER.info("Entered blueprint mode");
		}
		else {
			SimpleBlueprints.LOGGER.info("Exited blueprint mode");
		}
	}
}
