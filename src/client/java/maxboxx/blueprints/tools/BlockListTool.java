package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BlockListTool extends BlueprintTool {
	public BlockListTool() {
		super("block_list");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.empty();
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {

	}
}
