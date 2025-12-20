package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BlockGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BlueprintOptionsTool extends BlueprintTool {
	public BlueprintOptionsTool() {
		super("visibility");
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT  -> Optional.of(SimpleBlueprints.text("options.alpha"));
			case RIGHT -> Optional.of(SimpleBlueprints.text("options.color"));

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
