package maxboxx.blueprints;

import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.data.Color;
import maxboxx.blueprints.graphics.hud.BlueprintHud;
import maxboxx.blueprints.graphics.hud.HudRegistry;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.ToolAction;
import maxboxx.blueprints.tools.BlueprintTools;
import maxboxx.blueprints.tools.VisibilityTool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public class BlueprintManager {
	public static final int SLOT_COUNT = 9;

	private static final BlueprintHud HUD = new BlueprintHud();

	private static boolean active = false;
	private static BlueprintTool tool = null;

	private static int hotbarSlot = 0;
	private static int selectedSlot = 0;
	private static BlueprintSelection selection;
	private static BlueprintSelection[] selectionData = new BlueprintSelection[SLOT_COUNT];

	private static final BoxGraphic SELECTION_GRAPHIC = new BoxGraphic(WorldRenderer.FILLED_QUADS, false);
	private static final BoxGraphic SELECTION_OUTLINE = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, true);

	private static boolean showBlocks = true;
	private static Color blockColor = Color.WHITE;
	private static float blockAlpha = VisibilityTool.DEFAULT_ALPHA;

	private static class BlueprintSelection {
		private boolean isActive = false;
		private BlockPos min, max;

		private BlueprintData data = null;
		private BlueprintGraphic graphic = null;
	}

	static {
		for (int i = 0; i < selectionData.length; i++) {
			selectionData[i] = new BlueprintSelection();
		}

		selection = selectionData[0];
	}

	public static void init() {
		SELECTION_GRAPHIC.color  = Color.WHITE;
		SELECTION_GRAPHIC.color2 = new Color(0.5f, 0.9f, 1f);
		SELECTION_GRAPHIC.alpha  = 0.3f;

		SELECTION_OUTLINE.color  = Color.WHITE;
		SELECTION_OUTLINE.color2 = new Color(0.5f, 0.9f, 1f);
		SELECTION_OUTLINE.alpha  = 1f;

		HUD.hide();
		HudRegistry.register(HUD);

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (active) {
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

			while (KeyBinds.VISIBILITY.consumeClick()) {
				showBlocks = !showBlocks;

				for (BlueprintSelection selection : selectionData) {
					if (selection.graphic == null) continue;

					if (showBlocks && selection.data != null) {
						selection.graphic.setPosition(selection.min);
						WorldRenderer.addGraphic(selection.graphic);
					} else {
						WorldRenderer.removeGraphic(selection.graphic);
					}
				}
			}

			if (active) {
				updateMode();
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((a, d) -> {
			disable();
		});
	}

	public static boolean isActive() {
		return active;
	}

	public static BlueprintTool currentTool() {
		return tool;
	}

	public static void setToolSlot(int i) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		player.getInventory().setSelectedSlot(i);
	}

	public static int getBlueprintSlot() {
		return selectedSlot;
	}

	public static void setBlueprintSlot(int slot) {
		selectedSlot = slot;
		selection = selectionData[slot];

		updateGraphics();
	}

	private static void updateMode() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		tool = BlueprintTools.get(player.getInventory().getSelectedSlot());
		SELECTION_GRAPHIC.mode = tool.getGraphicMode();
		SELECTION_OUTLINE.mode = tool.getGraphicMode();
	}

	private static void toggleState() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		active = !active;

		if (active) {
			enable();
		}
		else {
			disable();
		}
	}

	private static void enable() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		active = true;
		HUD.setVisible(true);

		if (tool == null || (!hasData() && !hasSelection())) {
			tool = BlueprintTools.get(0);
		}

		hotbarSlot = player.getInventory().getSelectedSlot();

		player.getInventory().setSelectedSlot(BlueprintTools.indexOfSafe(tool));

		updateGraphics();
	}

	private static void disable() {
		active = false;
		HUD.setVisible(false);

		WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
		WorldRenderer.removeGraphic(SELECTION_OUTLINE);

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		player.getInventory().setSelectedSlot(hotbarSlot);
	}

	private static void handleAction(ToolAction action) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		if (!tool.isAvailable()) return;

		tool.performAction(player, action);
	}

	public static BlueprintData getData() {
		return selection.data;
	}

	public static boolean hasData() {
		return selection.data != null;
	}

	public static boolean hasDataInSlot(int i) {
		if (i >= 0 && i < selectionData.length) {
			return selectionData[i].data != null;
		}

		return false;
	}

	public static boolean hasSelectionForSlot(int i) {
		if (i >= 0 && i < selectionData.length) {
			return selectionData[i].isActive;
		}

		return false;
	}

	public static boolean hasPlacedData() {
		return selection.isActive && selection.data != null;
	}

	public static boolean hasAnyPlacedData() {
		for (BlueprintSelection selection : selectionData) {
			if (selection.isActive && selection.data != null) {
				return true;
			}
		}

		return false;
	}

	public static void setData(BlueprintData data) {
		BlueprintManager.selection.data = data;
	}

	public static BlockPos getSelectionMin() {
		return selection.min;
	}

	public static BlockPos getSelectionMax() {
		return selection.max;
	}

	public static Vec3i getSelectionSize() {
		return new Vec3i(
			selection.max.getX() - selection.min.getX() + 1,
			selection.max.getY() - selection.min.getY() + 1,
			selection.max.getZ() - selection.min.getZ() + 1
		);
	}

	public static boolean hasSelection() {
		return selection.isActive;
	}

	public static void addToSelection(BlockPos pos) {
		if (!selection.isActive) {
			selection.min = pos;
			selection.max = pos;

			selection.isActive = true;
		}
		else {
			selection.min = BlockPos.min(selection.min, pos);
			selection.max = BlockPos.max(selection.max, pos);
		}

		updateGraphics();
	}

	public static void clearSelection() {
		selection.isActive = false;
		updateGraphics();
	}

	public static void setBlockGraphic(BlueprintGraphic graphic) {
		if (selection.graphic != null) {
			WorldRenderer.removeGraphic(selection.graphic);
		}

		selection.graphic = graphic;
		graphic.setPosition(selection.min);

		if (showBlocks) {
			WorldRenderer.addGraphic(graphic);
		}

		if (selection.graphic != null) {
			selection.graphic.setTint(blockColor);
			selection.graphic.setAlpha(blockAlpha);
		}
	}

	public static void clearBlockGraphic() {
		if (selection.graphic != null) {
			WorldRenderer.removeGraphic(selection.graphic);
			selection.graphic = null;
		}
	}

	public static void setBlockAlpha(float alpha) {
		blockAlpha = alpha;

		for (BlueprintSelection selection : selectionData) {
			if (selection.graphic != null) {
				selection.graphic.setAlpha(alpha);
			}
		}
	}

	public static float getBlockAlpha() {
		return blockAlpha;
	}

	public static void setBlockColor(Color color) {
		blockColor = color;

		for (BlueprintSelection selection : selectionData) {
			if (selection.graphic != null) {
				selection.graphic.setTint(color);
			}
		}
	}

	public static Color getBlockColor() {
		return blockColor;
	}

	public static int getBlockLayer() {
		if (selection.graphic != null) {
			return selection.graphic.getSelectedLayer();
		}

		return 0;
	}

	public static void setBlockLayer(int layer) {
		if (selection.graphic != null) {
			selection.graphic.setSelectedLayer(layer);
		}
	}

	public static BlueprintGraphic.LayerMode getBlockLayerMode() {
		if (selection.graphic != null) {
			return selection.graphic.getLayerMode();
		}

		return BlueprintGraphic.LayerMode.SHOW_ALL;
	}

	public static void setBlockLayerMode(BlueprintGraphic.LayerMode mode) {
		if (selection.graphic != null) {
			selection.graphic.setLayerMode(mode);
		}
	}

	public static void moveSelection(Direction direction, int steps) {
		if (!selection.isActive) return;

		selection.min = selection.min.relative(direction, steps);
		selection.max = selection.max.relative(direction, steps);

		updateGraphics();
	}

	public static void expandSelection(Direction direction, int steps) {
		if (!selection.isActive) return;

		if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
			selection.max = selection.max.relative(direction, steps);
		}
		else {
			selection.min = selection.min.relative(direction, steps);
		}

		updateGraphics();
	}

	public static void shrinkSelection(Direction direction, int steps) {
		if (!selection.isActive) return;

		if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
			selection.max = BlockPos.max(selection.min, selection.max.relative(direction, steps));
		}
		else {
			selection.min = BlockPos.min(selection.max, selection.min.relative(direction, steps));
		}

		updateGraphics();
	}

	public static void rotateSelection() {
		if (selection.data == null) return;

		selection.data.rotate();
		selection.graphic.setRotation(selection.data.getRotation());

		selection.max = selection.min.offset(selection.data.transformedSize()).offset(-1, -1, -1);

		updateGraphics();
	}

	public static void mirrorSelection(Direction.Axis axis) {
		if (selection.data == null) return;
		if (axis == Direction.Axis.Y) return;

		selection.data.mirror(axis);
		selection.graphic.setMirror(selection.data.getMirror());
		selection.graphic.setRotation(selection.data.getRotation());

		selection.max = selection.min.offset(selection.data.transformedSize()).offset(-1, -1, -1);

		updateGraphics();
	}

	private static void updateGraphics() {
		if (!selection.isActive) {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);

			if (!selection.isActive && selection.graphic != null) {
				WorldRenderer.removeGraphic(selection.graphic);
			}

			return;
		}

		if (selection.graphic != null && showBlocks) {
			selection.graphic.setPosition(selection.min);
			WorldRenderer.addGraphic(selection.graphic);
		}

		WorldRenderer.addGraphic(SELECTION_GRAPHIC);
		WorldRenderer.addGraphic(SELECTION_OUTLINE);

		SELECTION_GRAPHIC.setMin(selection.min.getX() - 0.001f, selection.min.getY() - 0.001f, selection.min.getZ() - 0.001f);
		SELECTION_GRAPHIC.setMax(selection.max.getX() + 1.001f, selection.max.getY() + 1.001f, selection.max.getZ() + 1.001f);

		SELECTION_OUTLINE.setMin(selection.min.getX(), selection.min.getY(), selection.min.getZ());
		SELECTION_OUTLINE.setMax(selection.max.getX() + 1, selection.max.getY() + 1, selection.max.getZ() + 1);
	}
}
