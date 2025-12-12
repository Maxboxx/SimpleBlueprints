package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ResizeTool extends BlueprintTool {
	private boolean targetCloserSide = true;

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.of(switch (action) {
			case LEFT   -> SimpleBlueprints.text(targetCloserSide ? "resize.shrink" : "resize.expand");
			case RIGHT  -> SimpleBlueprints.text(targetCloserSide ? "resize.expand" : "resize.shrink");
			case MIDDLE -> SimpleBlueprints.text("resize.switch");
		});
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (action == ToolAction.MIDDLE) {
			targetCloserSide = !targetCloserSide;
			return;
		}

		Direction side = targetCloserSide ? player.getNearestViewDirection().getOpposite() : player.getNearestViewDirection();
		boolean moveAway = action == ToolAction.LEFT;

		if (moveAway == targetCloserSide) {
			BlueprintManager.shrinkSelection(side.getOpposite(), 1);
		}
		else {
			BlueprintManager.expandSelection(side, 1);
		}
	}
}
