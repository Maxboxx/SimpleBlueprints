package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class MoveTool extends BlueprintTool {
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
		switch (action) {
			case LEFT  -> BlueprintManager.moveSelection(player.getNearestViewDirection(), 1);
			case RIGHT -> BlueprintManager.moveSelection(player.getNearestViewDirection().getOpposite(), 1);
		}
	}
}
