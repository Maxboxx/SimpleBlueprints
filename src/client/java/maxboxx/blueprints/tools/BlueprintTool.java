package maxboxx.blueprints.tools;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public abstract class BlueprintTool {
	public abstract Optional<Component> getAction(ToolAction action);
	public abstract void performAction(LocalPlayer player, ToolAction action);
}
