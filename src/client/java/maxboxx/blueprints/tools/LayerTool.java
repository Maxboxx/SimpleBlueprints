package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class LayerTool extends BlueprintTool {
	public LayerTool() {
		super("layers");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.getData() != null;
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.of(switch (action) {
			case LEFT -> SimpleBlueprints.text("layers.away");
			case RIGHT -> SimpleBlueprints.text("layers.closer");
			case MIDDLE -> SimpleBlueprints.text("layers.mode");
		});
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		if (BlueprintManager.getData() == null) return;

		boolean facingUp = player.getForward().y() > 0;

		switch (action) {
			case LEFT  -> BlueprintManager.setBlockLayer(BlueprintManager.getBlockLayer() + (facingUp ? 1 : -1));
			case RIGHT -> BlueprintManager.setBlockLayer(BlueprintManager.getBlockLayer() + (facingUp ? -1 : 1));

			case MIDDLE -> {
				BlueprintManager.setBlockLayerMode(switch (BlueprintManager.getBlockLayerMode()) {
					case SHOW_ALL -> BlueprintGraphic.LayerMode.SHOW_BELOW;
					case SHOW_BELOW -> BlueprintGraphic.LayerMode.SHOW_ABOVE;
					case SHOW_ABOVE -> BlueprintGraphic.LayerMode.SHOW_SELECTED;
					case SHOW_SELECTED -> BlueprintGraphic.LayerMode.SHOW_ALL;
				});
			}
		}
	}
}
