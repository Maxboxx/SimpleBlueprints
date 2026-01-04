package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class VisibilityTool extends BlueprintTool {
	public VisibilityTool() {
		super("visibility");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasAnyPlacedData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT   -> Optional.of(SimpleBlueprints.text("visibility.alpha"));
			case RIGHT  -> Optional.of(SimpleBlueprints.text("visibility.color"));

			case MIDDLE -> {
				if (BlueprintManager.hasPlacedData()) {
					yield Optional.of(SimpleBlueprints.text(BlueprintManager.isVisible() ? "visibility.hide" : "visibility.show"));
				}
				else {
					yield Optional.empty();
				}
			}
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT  -> BlueprintManager.cycleBlockAlpha();
			case RIGHT -> BlueprintManager.cycleBlockColor();

			case MIDDLE -> {
				if (BlueprintManager.hasPlacedData()) {
					BlueprintManager.setVisibility(!BlueprintManager.isVisible());
				}
			}
		}
	}
}
