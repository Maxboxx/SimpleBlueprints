package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class RotateTool extends BlueprintTool {
	public RotateTool() {
		super("rotate");
	}

	@Override
	public BoxGraphic.Mode getGraphicMode() {
		return BoxGraphic.Mode.AXIS;
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasPlacedData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT  -> Optional.of(SimpleBlueprints.text("rotate.rotate"));
			case RIGHT -> Optional.of(SimpleBlueprints.text("rotate.mirror"));

			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (BlueprintManager.getData() == null) return;

		switch (action) {
			case LEFT  -> {
				BlueprintManager.rotateSelection();
			}

			case RIGHT -> {
				BlueprintManager.mirrorSelection(player.getNearestViewDirection().getAxis());
			}

			case MIDDLE -> {

			}
		}
	}
}
