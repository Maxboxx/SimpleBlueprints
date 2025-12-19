package maxboxx.blueprints;

import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.graphics.hud.BlueprintHud;
import maxboxx.blueprints.graphics.hud.HudRegistry;
import maxboxx.blueprints.graphics.world.BlockGraphic;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.ToolAction;
import maxboxx.blueprints.tools.BlueprintTools;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class BlueprintManager {
	private static final BlueprintHud HUD = new BlueprintHud();

	private static boolean isActive = false;
	private static BlueprintTool tool = null;

	private static boolean selectionActive = false;
	private static BlockPos selectionMin, selectionMax;

	private static BlueprintData data = null;

	private static final BoxGraphic SELECTION_GRAPHIC = new BoxGraphic(WorldRenderer.FILLED, false);
	private static final BoxGraphic SELECTION_OUTLINE = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, true);

	private static BlockGraphic blockGraphic = null;
	private static float blockAlpha = 0.5f;

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
					handleAction(ToolAction.LEFT);
				}
				else if (rightClick) {
					handleAction(ToolAction.RIGHT);
				}
				else if (middleClick) {
					handleAction(ToolAction.MIDDLE);
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (KeyBinds.TOGGLE.consumeClick()) {
				toggleState();
			}

			if (isActive) {
				updateMode();
			}
		});
	}

	public static BlueprintTool currentTool() {
		return tool;
	}

	private static void updateMode() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		tool = BlueprintTools.get(player.getInventory().getSelectedSlot());
	}

	private static void toggleState() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		isActive = !isActive;
		HUD.setVisible(isActive);

		if (isActive) {
			tool = BlueprintTools.get(0);

			player.getInventory().setSelectedSlot(BlueprintTools.indexOf(tool));

			updateGraphics();
		}
		else {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);
		}
	}

	private static void handleAction(ToolAction action) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		tool.performAction(player, action);
	}

	public static BlueprintData getData() {
		return data;
	}

	public static void setData(BlueprintData data) {
		BlueprintManager.data = data;
	}

	public static BlockPos getSelectionMin() {
		return selectionMin;
	}

	public static BlockPos getSelectionMax() {
		return selectionMax;
	}

	public static Vec3i getSelectionSize() {
		return new Vec3i(
			selectionMax.getX() - selectionMin.getX() + 1,
			selectionMax.getY() - selectionMin.getY() + 1,
			selectionMax.getZ() - selectionMin.getZ() + 1
		);
	}

	public static boolean hasSelection() {
		return selectionActive;
	}

	public static void addToSelection(BlockPos pos) {
		if (!selectionActive) {
			selectionMin = pos;
			selectionMax = pos;

			selectionActive = true;
		}
		else {
			selectionMin = BlockPos.min(selectionMin, pos);
			selectionMax = BlockPos.max(selectionMax, pos);
		}

		updateGraphics();
	}

	public static void clearSelection() {
		selectionActive = false;
		updateGraphics();
	}

	public static void setBlockGraphic(BlockGraphic graphic) {
		if (blockGraphic != null) {
			WorldRenderer.removeGraphic(blockGraphic);
		}

		blockGraphic = graphic;
		graphic.setPosition(selectionMin);
		WorldRenderer.addGraphic(graphic);

		if (blockGraphic != null) {
			blockGraphic.setAlpha(blockAlpha);
		}
	}

	public static void clearBlockGraphic() {
		if (blockGraphic != null) {
			WorldRenderer.removeGraphic(blockGraphic);
			blockGraphic = null;
		}
	}

	public static void setBlockAlpha(float alpha) {
		blockAlpha = alpha;

		if (blockGraphic != null) {
			blockGraphic.setAlpha(alpha);
		}
	}

	public static float getBlockAlpha() {
		return blockAlpha;
	}

	public static void moveSelection(Direction direction, int steps) {
		if (!selectionActive) return;

		selectionMin = selectionMin.relative(direction, steps);
		selectionMax = selectionMax.relative(direction, steps);

		updateGraphics();
	}

	public static void expandSelection(Direction direction, int steps) {
		if (!selectionActive) return;

		if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
			selectionMax = selectionMax.relative(direction, steps);
		}
		else {
			selectionMin = selectionMin.relative(direction, steps);
		}

		updateGraphics();
	}

	public static void shrinkSelection(Direction direction, int steps) {
		if (!selectionActive) return;

		if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
			selectionMax = BlockPos.max(selectionMin, selectionMax.relative(direction, steps));
		}
		else {
			selectionMin = BlockPos.min(selectionMax, selectionMin.relative(direction, steps));
		}

		updateGraphics();
	}

	private static void updateGraphics() {
		if (!selectionActive) {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);

			if (blockGraphic != null) {
				WorldRenderer.removeGraphic(blockGraphic);
			}

			return;
		}

		if (blockGraphic != null) {
			blockGraphic.setPosition(selectionMin);
			WorldRenderer.addGraphic(blockGraphic);
		}

		WorldRenderer.addGraphic(SELECTION_GRAPHIC);
		WorldRenderer.addGraphic(SELECTION_OUTLINE);

		SELECTION_GRAPHIC.setMin(selectionMin.getX() - 0.001f, selectionMin.getY() - 0.001f, selectionMin.getZ() - 0.001f);
		SELECTION_GRAPHIC.setMax(selectionMax.getX() + 1.001f, selectionMax.getY() + 1.001f, selectionMax.getZ() + 1.001f);

		SELECTION_OUTLINE.setMin(selectionMin.getX(), selectionMin.getY(), selectionMin.getZ());
		SELECTION_OUTLINE.setMax(selectionMax.getX() + 1, selectionMax.getY() + 1, selectionMax.getZ() + 1);
	}
}
