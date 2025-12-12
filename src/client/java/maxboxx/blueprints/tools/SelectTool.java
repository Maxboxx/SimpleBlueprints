package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class SelectTool extends BlueprintTool {
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
						BlueprintManager.addToSelection(blockHit.getBlockPos());
					}
				}
			}

			case RIGHT -> {
				BlueprintManager.addToSelection(player.blockPosition());
			}

			case MIDDLE -> {
				BlueprintManager.clearSelection();
			}
		}
	}
}
