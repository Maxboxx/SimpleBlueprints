package maxboxx.blueprints;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import maxboxx.blueprints.graphics.hud.BlueprintHud;
import maxboxx.blueprints.graphics.hud.HudRegistry;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;

public class BlueprintManager {
	private static final BlueprintHud HUD = new BlueprintHud();

	private static boolean isActive = false;

	private static BlockPos selection;

	private static final BoxGraphic SELECTION_GRAPHIC = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, false);
	private static final BoxGraphic SELECTION_OUTLINE = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, true);

	public static void init() {
		SELECTION_GRAPHIC.red   = 1f;
		SELECTION_GRAPHIC.green = 1f;
		SELECTION_GRAPHIC.blue  = 1f;
		SELECTION_GRAPHIC.alpha = 0.3f;

		SELECTION_OUTLINE.red   = 1f;
		SELECTION_OUTLINE.green = 1f;
		SELECTION_OUTLINE.blue  = 1f;
		SELECTION_OUTLINE.alpha = 1f;

		HUD.hide();
		HudRegistry.register(HUD);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (isActive) {
				boolean leftClick  = KeyBinds.consume(client.options.keyAttack);
				boolean rightClick = KeyBinds.consume(client.options.keyUse);

				if (leftClick) {
					handleLeftClick();
				}

				if (rightClick) {
					handleRightClick();
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (KeyBinds.TOGGLE.consumeClick()) {
				toggleState();
			}
		});
	}

	private static void toggleState() {
		isActive = !isActive;
		HUD.setVisible(isActive);

		if (isActive) {
			LocalPlayer player = Minecraft.getInstance().player;

			if (player == null) return;

			selection = player.blockPosition();

			SELECTION_GRAPHIC.minX = selection.getX();
			SELECTION_GRAPHIC.minY = selection.getY();
			SELECTION_GRAPHIC.minZ = selection.getZ();
			SELECTION_GRAPHIC.maxX = selection.getX() + 1;
			SELECTION_GRAPHIC.maxY = selection.getY() + 1;
			SELECTION_GRAPHIC.maxZ = selection.getZ() + 1;

			SELECTION_OUTLINE.minX = selection.getX();
			SELECTION_OUTLINE.minY = selection.getY();
			SELECTION_OUTLINE.minZ = selection.getZ();
			SELECTION_OUTLINE.maxX = selection.getX() + 1;
			SELECTION_OUTLINE.maxY = selection.getY() + 1;
			SELECTION_OUTLINE.maxZ = selection.getZ() + 1;

			WorldRenderer.addGraphic(SELECTION_GRAPHIC);
			WorldRenderer.addGraphic(SELECTION_OUTLINE);
		}
		else {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);
		}
	}

	private static void handleLeftClick() {
		SimpleBlueprints.LOGGER.info("Left Click");
	}

	private static void handleRightClick() {
		SimpleBlueprints.LOGGER.info("Right Click");
	}
}
