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
import net.minecraft.core.Direction;

public class BlueprintManager {
	private static final BlueprintHud HUD = new BlueprintHud();

	private static boolean isActive = false;
	private static Mode mode = Mode.NUDGE;

	private static BlockPos selectionMin, selectionMax;

	private static final BoxGraphic SELECTION_GRAPHIC = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, false);
	private static final BoxGraphic SELECTION_OUTLINE = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, true);

	public static void init() {
		SELECTION_GRAPHIC.setColor(1f, 1f, 1f, 0.3f);
		SELECTION_OUTLINE.setColor(1f, 1f, 1f, 1f);

		HUD.hide();
		HudRegistry.register(HUD);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (isActive) {
				boolean leftClick   = KeyBinds.consume(client.options.keyAttack);
				boolean rightClick  = KeyBinds.consume(client.options.keyUse);
				boolean middleClick = KeyBinds.consume(client.options.keyPickItem);

				if (leftClick) {
					handleLeftClick();
				}

				if (rightClick) {
					handleRightClick();
				}

				if (middleClick) {
					handleMiddleClick();
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
			mode = Mode.NUDGE;

			LocalPlayer player = Minecraft.getInstance().player;

			if (player == null) return;

			selectionMin = player.blockPosition();
			selectionMax = selectionMin;

			updateGraphics();

			WorldRenderer.addGraphic(SELECTION_GRAPHIC);
			WorldRenderer.addGraphic(SELECTION_OUTLINE);
		}
		else {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);
		}
	}

	private static void handleLeftClick() {
		handleClick(false);
	}

	private static void handleRightClick() {
		handleClick(true);
	}

	private static void handleClick(boolean invert) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		Direction viewDir = player.getNearestViewDirection();

		switch (mode) {
			case NUDGE  -> nudge(viewDir, invert);
			case EXPAND -> expand(viewDir, invert);
		}
	}

	private static void nudge(Direction viewDir, boolean invert) {
		Direction nudgeDir = invert ? viewDir.getOpposite() : viewDir;

		selectionMin = selectionMin.relative(nudgeDir);
		selectionMax = selectionMax.relative(nudgeDir);

		updateGraphics();
	}

	private static void expand(Direction viewDir, boolean invert) {
		if (invert) {
			if (viewDir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
				selectionMax = selectionMax.relative(viewDir.getOpposite());
			}
			else {
				selectionMin = selectionMin.relative(viewDir.getOpposite());
			}
		}
		else {
			if (viewDir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
				selectionMax = BlockPos.max(selectionMin, selectionMax.relative(viewDir));
			}
			else {
				selectionMin = BlockPos.min(selectionMax, selectionMin.relative(viewDir));
			}
		}

		updateGraphics();
	}

	private static void handleMiddleClick() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		switch (mode) {
			case NUDGE  -> mode = Mode.EXPAND;
			case EXPAND -> mode = Mode.NUDGE;
		};
	}

	private static void updateGraphics() {
		SELECTION_GRAPHIC.setMin(selectionMin.getX(), selectionMin.getY(), selectionMin.getZ());
		SELECTION_GRAPHIC.setMax(selectionMax.getX() + 1, selectionMax.getY() + 1, selectionMax.getZ() + 1);

		SELECTION_OUTLINE.setMin(selectionMin.getX(), selectionMin.getY(), selectionMin.getZ());
		SELECTION_OUTLINE.setMax(selectionMax.getX() + 1, selectionMax.getY() + 1, selectionMax.getZ() + 1);
	}

	private enum Mode {
		NUDGE,
		EXPAND
	}
}
