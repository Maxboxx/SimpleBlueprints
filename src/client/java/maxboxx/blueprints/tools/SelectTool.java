package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintBlockData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

public class SelectTool extends BlueprintTool {
	private static final Identifier PLACE_ICON = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/place");

	public SelectTool() {
		super("select");
	}

	@Override
	public Identifier getIcon() {
		return BlueprintManager.hasData() ? PLACE_ICON : super.getIcon();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		if (!BlueprintManager.hasSelection() && action == ToolAction.MIDDLE) {
			return Optional.empty();
		}

		if (BlueprintManager.getData() == null) {
			return Optional.of(switch (action) {
				case LEFT   -> SimpleBlueprints.text("select.target");
				case RIGHT  -> SimpleBlueprints.text("select.player");
				case MIDDLE -> SimpleBlueprints.text("select.clear");
			});
		}

		return Optional.of(switch (action) {
			case LEFT   -> SimpleBlueprints.text("select.target_place");
			case RIGHT  -> SimpleBlueprints.text("select.player_place");
			case MIDDLE -> SimpleBlueprints.text("select.clear_place");
		});
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				HitResult hit = player.pick(64, 0, false);

				if (hit instanceof BlockHitResult blockHit) {
					if (blockHit.getType() == HitResult.Type.BLOCK) {
						selectPosition(blockHit.getBlockPos(), blockHit.getDirection());
					}
				}
			}

			case RIGHT -> {
				selectPosition(player.blockPosition(), Direction.UP);
			}

			case MIDDLE -> {
				BlueprintManager.clearSelection();
			}
		}
	}

	private void selectPosition(BlockPos pos, Direction normal) {
		BlueprintBlockData data = BlueprintManager.getData();

		if (data == null) {
			BlueprintManager.addToSelection(pos);
			return;
		}

		BlueprintManager.clearSelection();

		Vec3i halfSize = data.transformedHalfSize();

		BlockPos placePos = switch (normal) {
			case UP    -> pos.offset(-halfSize.getX(), 1, -halfSize.getZ());
			case DOWN  -> pos.offset(-halfSize.getX(), -data.transformedSize().getY(), -halfSize.getZ());
			case WEST  -> pos.offset(-data.transformedSize().getX(), -halfSize.getY(), -halfSize.getZ());
			case EAST  -> pos.offset(1, -halfSize.getY(), -halfSize.getZ());
			case NORTH -> pos.offset(-halfSize.getX(), -halfSize.getY(), -data.transformedSize().getZ());
			case SOUTH -> pos.offset(-halfSize.getX(), -halfSize.getY(), 1);
		};

		BlueprintManager.addToSelection(placePos);
		BlueprintManager.addToSelection(placePos.offset(data.transformedSize()).offset(-1, -1, -1));
	}
}
