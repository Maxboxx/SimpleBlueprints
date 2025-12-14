package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.graphics.world.BlockGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClipboardTool extends BlueprintTool {
	private BlueprintData data;

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT   -> Optional.of(SimpleBlueprints.text("clipboard.copy"));
			case RIGHT  -> Optional.of(SimpleBlueprints.text("clipboard.paste"));
			case MIDDLE -> Optional.of(SimpleBlueprints.text("clipboard.clear"));
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (!BlueprintManager.hasSelection()) return;

		switch (action) {
			case LEFT -> {
				data = new BlueprintData();

				data.loadFromWorld(
					player.level(),
					BlueprintManager.getSelectionMin(),
					BlueprintManager.getSelectionSize()
				);

				BlueprintManager.setData(data);
			}

			case RIGHT -> {
				if (data != null) {
					data.placeInWorld(
						player,
						BlueprintManager.getSelectionMin()
					);
				}
			}

			case MIDDLE -> {
				data = null;
				BlueprintManager.setData(null);
			}
		}
	}
}
