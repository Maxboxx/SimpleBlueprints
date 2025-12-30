package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClipboardTool extends BlueprintTool {
	public ClipboardTool() {
		super("clipboard");
	}

	@Override
	public boolean isAvailable() {
		if (BlueprintManager.getData() != null) {
			return true;
		}

		return BlueprintManager.hasSelection();
	}

	@Override
	public int getColor() {
		if (BlueprintManager.getData() != null) {
			return BLUE;
		}

		return super.getColor();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT -> {
				if (BlueprintManager.hasSelection()) {
					yield Optional.of(SimpleBlueprints.text("clipboard.copy"));
				}
				else {
					yield Optional.empty();
				}
			}

			case RIGHT -> {
				if (!BlueprintManager.hasSelection()) yield Optional.empty();

				if (BlueprintManager.getData() != null && Minecraft.getInstance().player != null && BlueprintManager.getData().canPlace(Minecraft.getInstance().player)) {
					yield Optional.of(SimpleBlueprints.text("clipboard.paste"));
				}
				else {
					yield Optional.empty();
				}
			}

			case MIDDLE -> {
				if (BlueprintManager.getData() != null) {
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
		switch (action) {
			case LEFT -> {
				if (!BlueprintManager.hasSelection()) break;

				BlueprintData data = new BlueprintData();

				data.loadFromWorld(
					player.level(),
					BlueprintManager.getSelectionMin(),
					BlueprintManager.getSelectionSize()
				);

				BlueprintManager.setData(data);
				BlueprintManager.setBlockGraphic(data.toGraphic(player.level()));
			}

			case RIGHT -> {
				if (!BlueprintManager.hasSelection()) break;

				if (BlueprintManager.getData() != null && BlueprintManager.getData().canPlace(player)) {
					BlueprintManager.getData().placeInWorld(
						player,
						BlueprintManager.getSelectionMin()
					);
				}
			}

			case MIDDLE -> {
				BlueprintManager.setData(null);
				BlueprintManager.clearBlockGraphic();
				BlueprintManager.clearSelection();
			}
		}
	}
}
