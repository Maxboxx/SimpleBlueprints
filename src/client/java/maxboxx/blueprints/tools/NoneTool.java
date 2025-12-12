package maxboxx.blueprints.tools;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class NoneTool extends BlueprintTool {
	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.empty();
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {

	}
}
