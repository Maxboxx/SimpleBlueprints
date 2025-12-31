package maxboxx.blueprints.tools;

import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class MultiTool extends BlueprintTool {
	public MultiTool() {
		super((Identifier)null);
	}

	protected abstract @NotNull BlueprintTool getTool();

	@Override
	public Identifier getIcon() {
		return getTool().getIcon();
	}

	@Override
	public int getColor() {
		return getTool().getColor();
	}

	@Override
	public BoxGraphic.Mode getGraphicMode() {
		return getTool().getGraphicMode();
	}

	@Override
	public boolean isAvailable() {
		return getTool().isAvailable();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return getTool().getAction(action);
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		getTool().performAction(player, action);
	}
}
