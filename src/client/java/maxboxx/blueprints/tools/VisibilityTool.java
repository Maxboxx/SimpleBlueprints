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
		return BlueprintManager.hasSelection() && BlueprintManager.getData() != null;
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT  -> Optional.of(SimpleBlueprints.text("visibility.alpha"));
			case RIGHT -> Optional.of(SimpleBlueprints.text("visibility.color"));

			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				float alpha = BlueprintManager.getBlockAlpha();

				alpha += 0.25f;

				if (alpha > 1f) {
					alpha = 0f;
				}

				BlueprintManager.setBlockAlpha(alpha);
			}

			case RIGHT -> {

			}
		}
	}
}
