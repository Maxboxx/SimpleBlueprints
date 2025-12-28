package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.Color;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class VisibilityTool extends BlueprintTool {
	private static final float ALPHA_STEP_SIZE = 0.2f;

	public static final float DEFAULT_ALPHA = ALPHA_STEP_SIZE * 3f;

	private static final Color[] COLORS = {
		Color.WHITE,
		new Color(0.5f, 1f, 1f),
		new Color(1f, 1f, 0f),
		new Color(1f, 0.7f, 1f)
	};

	private int colorIndex = 0;

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

				alpha -= ALPHA_STEP_SIZE;

				if (alpha < 0f) {
					alpha = 1f;
				}

				BlueprintManager.setBlockAlpha(alpha);
			}

			case RIGHT -> {
				colorIndex++;

				if (colorIndex >= COLORS.length) {
					colorIndex = 0;
				}

				BlueprintManager.setBlockColor(COLORS[colorIndex]);
			}
		}
	}
}
