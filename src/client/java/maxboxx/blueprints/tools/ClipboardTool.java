package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.*;

public class ClipboardTool extends BlueprintTool {
	private BlueprintData data;

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT  -> Optional.of(SimpleBlueprints.text("clipboard.copy"));
			case RIGHT -> Optional.of(SimpleBlueprints.text("clipboard.paste"));

			default -> Optional.empty();
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
			}

			case RIGHT -> {
				if (data != null) {
					data.placeInWorld(
						player,
						BlueprintManager.getSelectionMin()
					);
				}
			}
		}
	}
}
