package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ResizeTool extends BlueprintTool {
	private boolean targetCloserSide = true;

	public ResizeTool() {
		super("resize");
	}

	@Override
	public BoxGraphic.Mode getGraphicMode() {
		return targetCloserSide ? BoxGraphic.Mode.CLOSE_FACE : BoxGraphic.Mode.FAR_FACE;
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasSelection() && BlueprintManager.getData() == null;
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		if (BlueprintManager.getData() != null) {
			return Optional.empty();
		}

		return Optional.of(switch (action) {
			case LEFT   -> SimpleBlueprints.text(targetCloserSide ? "resize.shrink" : "resize.expand");
			case RIGHT  -> SimpleBlueprints.text(targetCloserSide ? "resize.expand" : "resize.shrink");
			case MIDDLE -> SimpleBlueprints.text("resize.switch");
		});
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (BlueprintManager.getData() != null) {
			return;
		}

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
