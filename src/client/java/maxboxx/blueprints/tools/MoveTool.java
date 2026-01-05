package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.KeyBinds;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class MoveTool extends BlueprintTool {
	public MoveTool() {
		super("move");
	}

	@Override
	public BoxGraphic.Mode getGraphicMode() {
		return BoxGraphic.Mode.AXIS;
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasSelection();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT  -> Optional.of(SimpleBlueprints.text("move.away"));
			case RIGHT -> Optional.of(SimpleBlueprints.text("move.closer"));

			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		int steps = 1;

		if (KeyBinds.isShiftOrCtrlDown()) {
			steps = 5;
		}

		switch (action) {
			case LEFT  -> BlueprintManager.moveSelection(player.getNearestViewDirection(), steps);
			case RIGHT -> BlueprintManager.moveSelection(player.getNearestViewDirection().getOpposite(), steps);
		}
	}
}
