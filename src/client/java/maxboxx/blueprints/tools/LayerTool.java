package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class LayerTool extends BlueprintTool {
	public LayerTool() {
		super("layers");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasPlacedData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return Optional.of(switch (action) {
			case LEFT -> Txt.key("layers.away");
			case RIGHT -> Txt.key("layers.closer");
			case MIDDLE -> Txt.key("layers.mode", BlueprintManager.getBlockLayerMode().getText());
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
					case SHOW_ABOVE -> BlueprintGraphic.LayerMode.SHOW_SLICE;
					case SHOW_SLICE -> BlueprintGraphic.LayerMode.SHOW_ALL;
				});
			}
		}
	}
}
