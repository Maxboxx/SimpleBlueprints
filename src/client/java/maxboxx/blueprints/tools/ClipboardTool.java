package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.utils.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClipboardTool extends BlueprintTool {
	private BlockUtil.PlaceMode pasteMode = BlockUtil.PlaceMode.REPLACE_ALL;

	public ClipboardTool() {
		super("clipboard");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasData() || BlueprintManager.hasSelection();
	}

	@Override
	public int getColor() {
		if (BlueprintManager.hasData()) {
			return BLUE;
		}

		return super.getColor();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT -> {
				if (BlueprintManager.hasData()) {
					yield Optional.of(SimpleBlueprints.text("clipboard.paste_mode", pasteMode.asText()));
				}
				else if (BlueprintManager.hasSelection()) {
					yield Optional.of(SimpleBlueprints.text("clipboard.copy"));
				}
				else {
					yield Optional.empty();
				}
			}

			case RIGHT -> {
				if (!BlueprintManager.hasSelection()) yield Optional.empty();

				if (BlueprintManager.hasData() && Minecraft.getInstance().player != null && BlueprintManager.getData().canPlace(Minecraft.getInstance().player)) {
					yield Optional.of(SimpleBlueprints.text("clipboard.paste"));
				}
				else {
					yield Optional.empty();
				}
			}

			case MIDDLE -> {
				if (BlueprintManager.hasData()) {
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
				if (BlueprintManager.hasData()) {
					pasteMode = switch (pasteMode) {
						case REPLACE_ALL -> BlockUtil.PlaceMode.PLACE_IN_AIR;
						case PLACE_IN_AIR -> BlockUtil.PlaceMode.IGNORE_AIR;
						default -> BlockUtil.PlaceMode.REPLACE_ALL;
					};

					break;
				}

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

				if (BlueprintManager.hasData() && BlueprintManager.getData().canPlace(player)) {
					BlueprintManager.getData().placeInWorld(
						player,
						BlueprintManager.getSelectionMin(),
						pasteMode
					);
				}
			}

			case MIDDLE -> {
				if (!BlueprintManager.hasData()) break;

				BlueprintManager.setData(null);
				BlueprintManager.clearBlockGraphic();
				BlueprintManager.clearSelection();
			}
		}
	}
}
