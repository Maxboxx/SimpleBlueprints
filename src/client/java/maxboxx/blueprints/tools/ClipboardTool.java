package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ClipboardTool extends BlueprintTool {
	private BlueprintData data;

	public ClipboardTool() {
		super("clipboard");
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT   -> Optional.of(SimpleBlueprints.text("clipboard.copy"));
			case RIGHT  -> {
				if (data != null) {
					yield Optional.of(SimpleBlueprints.text("clipboard.paste"));
				}
				else {
					yield Optional.empty();
				}
			}

			case MIDDLE -> {
				if (data != null) {
					yield Optional.of(SimpleBlueprints.text("clipboard.clear"));
				}
				else {
					yield Optional.empty();
				}
			}
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
				BlueprintManager.setBlockGraphic(data.toGraphic(player.level()));
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
				BlueprintManager.clearBlockGraphic();
			}
		}
	}
}
