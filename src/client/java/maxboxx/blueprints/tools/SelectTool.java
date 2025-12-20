package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class SelectTool extends BlueprintTool {
	public SelectTool() {
		super("select");
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.of(switch (action) {
			case LEFT   -> SimpleBlueprints.text("select.target");
			case RIGHT  -> SimpleBlueprints.text("select.player");
			case MIDDLE -> SimpleBlueprints.text("select.clear");
		});
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				HitResult hit = player.pick(32, 0, false);

				if (hit instanceof BlockHitResult blockHit) {
					if (blockHit.getType() == HitResult.Type.BLOCK) {
						selectPosition(blockHit.getBlockPos());
					}
				}
			}

			case RIGHT -> {
				selectPosition(player.blockPosition());
			}

			case MIDDLE -> {
				BlueprintManager.clearSelection();
			}
		}
	}

	private void selectPosition(BlockPos pos) {
		BlueprintData data = BlueprintManager.getData();

		if (data == null) {
			BlueprintManager.addToSelection(pos);
			return;
		}

		BlueprintManager.clearSelection();
		BlueprintManager.addToSelection(pos);
		BlueprintManager.addToSelection(pos.offset(data.size()).offset(-1, -1, -1));
	}
}
