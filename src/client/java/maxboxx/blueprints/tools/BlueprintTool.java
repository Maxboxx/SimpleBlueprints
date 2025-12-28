package maxboxx.blueprints.tools;

import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.Optional;

public abstract class BlueprintTool {
	protected static final int WHITE = ARGB.color(255, 255, 255);
	protected static final int GRAY = ARGB.color(180, 180, 180);
	protected static final int GRAYED_OUT = ARGB.color(128, 128, 128, 128);
	protected static final int BLUE = ARGB.color(100, 200, 255);

	private final Identifier icon;

	protected BlueprintTool(String iconName) {
		if (iconName == null) {
			icon = null;
		}
		else {
			icon = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/" + iconName);
		}
	}

	protected BlueprintTool(Identifier icon) {
		this.icon = icon;
	}

	public Identifier getIcon() {
		return icon;
	}

	public int getColor() {
		return isAvailable() ? WHITE : GRAYED_OUT;
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
