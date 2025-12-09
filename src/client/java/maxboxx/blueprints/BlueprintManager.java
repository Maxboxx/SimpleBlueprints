package maxboxx.blueprints;

import maxboxx.blueprints.graphics.hud.BlueprintHud;
import maxboxx.blueprints.graphics.hud.HudRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BlueprintManager {
	private static boolean isActive = false;

	private static final BlueprintHud HUD = new BlueprintHud();

	public static void init() {
		HUD.hide();
		HudRegistry.register(HUD);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (isActive) {
				boolean leftClick  = KeyBinds.consume(client.options.keyAttack);
				boolean rightClick = KeyBinds.consume(client.options.keyUse);

				if (leftClick) {
					handleLeftClick();
				}

				if (rightClick) {
					handleRightClick();
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
		HUD.setVisible(isActive);
	}

	private static void handleLeftClick() {
		SimpleBlueprints.LOGGER.info("Left Click");
	}

	private static void handleRightClick() {
		SimpleBlueprints.LOGGER.info("Right Click");
	}
}
