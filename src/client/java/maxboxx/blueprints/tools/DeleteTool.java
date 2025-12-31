package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.utils.BlockUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;

public class DeleteTool extends BlueprintTool {
	public DeleteTool() {
		super("delete");
	}

	@Override
	public boolean isAvailable() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return false;

		return BlueprintManager.hasSelection() && player.isCreative();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case MIDDLE -> Optional.of(SimpleBlueprints.text("delete.clear"));
			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (!player.isCreative()) return;

		switch (action) {
			case MIDDLE -> {
				if (BlueprintManager.hasSelection()) {
					BlockUtil.fillBlocks(player, BlueprintManager.getSelectionMin(), BlueprintManager.getSelectionMax(), Blocks.AIR.defaultBlockState());
				}
			}
		}
	}
}
