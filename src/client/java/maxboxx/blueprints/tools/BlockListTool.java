package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BlockListTool extends BlueprintTool {
	public enum CountMode {
		TOTAL,
		STACKS,
		CHESTS
	}

	public BlockListTool() {
		super("block_list");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT -> switch (BlueprintManager.getBlockListMode()) {
				case TOTAL  -> Optional.of(SimpleBlueprints.text("block_list.show_stacks"));
				case STACKS -> Optional.of(SimpleBlueprints.text("block_list.show_chests"));
				case CHESTS -> Optional.of(SimpleBlueprints.text("block_list.show_total"));
			};

			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				BlueprintManager.setBlockListMode(switch (BlueprintManager.getBlockListMode()) {
					case TOTAL  -> CountMode.STACKS;
					case STACKS -> CountMode.CHESTS;

					default -> CountMode.TOTAL;
				});
			}
		}
	}
}
