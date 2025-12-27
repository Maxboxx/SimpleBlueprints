package maxboxx.blueprints.tools;

import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public abstract class BlueprintTool {
	public final Identifier ICON;

	protected BlueprintTool(String iconName) {
		if (iconName == null) {
			ICON = null;
		}
		else {
			ICON = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/" + iconName);
		}
	}

	public BoxGraphic.Mode getGraphicMode() {
		return BoxGraphic.Mode.NONE;
	}

	public boolean isAvailable() {
		return true;
	}

	public abstract Optional<Component> getAction(ToolAction action);
	public abstract void performAction(LocalPlayer player, ToolAction action);
}
