package maxboxx.blueprints;

import maxboxx.blueprints.data.BlueprintBlockData;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.data.Color;
import maxboxx.blueprints.data.SlotProperties;
import maxboxx.blueprints.graphics.ui.hud.BlueprintHud;
import maxboxx.blueprints.graphics.ui.hud.HudRegistry;
import maxboxx.blueprints.graphics.ui.screens.ItemListScreen;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.tools.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.nio.file.Path;

public class BlueprintManager {
	public static final int SLOT_COUNT = 9;

	private static final BlueprintHud HUD = new BlueprintHud();

	private static boolean active = false;
	private static BlueprintTool tool = null;
	private static boolean hasLoaded = false;

	private static int hotbarSlot = 0;
	private static int selectedSlot = 0;
	private static BlueprintData selection;
	private static final BlueprintData[] selectionData = new BlueprintData[SLOT_COUNT];

	private static final BoxGraphic SELECTION_GRAPHIC = new BoxGraphic(WorldRenderer.FILLED_QUADS, false);
	private static final BoxGraphic SELECTION_OUTLINE = new BoxGraphic(WorldRenderer.FILLED_NO_DEPTH, true);

	private static final float ALPHA_STEP_SIZE = 0.2f;
	private static final float DEFAULT_ALPHA = ALPHA_STEP_SIZE * 3f;

	private static final Color[] COLORS = {
		Color.WHITE,
		new Color(0.5f, 1f, 1f),
		new Color(1f, 1f, 0f),
		new Color(1f, 0.7f, 1f)
	};

	private static boolean showBlocks = true;
	private static Color blockColor = Color.WHITE;
	private static float blockAlpha = DEFAULT_ALPHA;
	private static int colorIndex = 0;

	private static ItemListTool.CountMode blockListMode = ItemListTool.CountMode.TOTAL;

	private static final SlotProperties slotProperties = new SlotProperties();

	static {
		for (int i = 0; i < selectionData.length; i++) {
			selectionData[i] = new BlueprintData();
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
				if (active) continue;

				showBlocks = !showBlocks;

				for (BlueprintData selection : selectionData) {
					if (selection.graphic == null) continue;

					if (showBlocks && selection.data != null && selection.isVisible) {
						selection.graphic.setPosition(selection.min);
						WorldRenderer.addGraphic(selection.graphic);
					}
					else {
						WorldRenderer.removeGraphic(selection.graphic);
					}
				}
			}

			while (KeyBinds.LAYER_UP.consumeClick()) {
				setBlockLayer(getBlockLayer() + 1);
			}

			while (KeyBinds.LAYER_DOWN.consumeClick()) {
				setBlockLayer(getBlockLayer() - 1);
			}

			while (KeyBinds.CHANGE_ALPHA.consumeClick()) {
				cycleBlockAlpha();
			}

			while (KeyBinds.CHANGE_COLOR.consumeClick()) {
				cycleBlockColor();
			}

			while (KeyBinds.EDIT_ITEMS.consumeClick()) {
				if (hasData()) {
					Minecraft.getInstance().setScreen(new ItemListScreen());
				}
			}

			if (active) {
				updateMode();
			}
		});

		ClientPlayConnectionEvents.INIT.register((a, d) -> {
			if (hasLoaded) return;

			Path configPath = SimpleBlueprints.configPath();

			slotProperties.load(configPath.resolve("slots.properties"));

			for (int i = 0; i < selectionData.length; i++) {
				Path path = configPath.resolve("selection" + (i + 1) + ".dat");
				selectionData[i].loadData(path);

				setupData(selectionData[i]);
			}

			hasLoaded = true;
		});

		ClientPlayConnectionEvents.DISCONNECT.register((a, d) -> {
			disable();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register((a) -> {
			if (!hasLoaded) return;

			Path configPath = SimpleBlueprints.configPath();

			for (int i = 0; i < selectionData.length; i++) {
				Path path = configPath.resolve("selection" + (i + 1) + ".dat");

				if (!slotProperties.getData(i).persistent()) {
					path.toFile().delete();
					continue;
				}

				if (!selectionData[i].isDirty()) continue;

				selectionData[i].saveData(path, false);

				if (selectionData[i].graphic != null) {
					WorldRenderer.removeGraphic(selectionData[i].graphic);
				}
			}
		});
	}

	public static boolean isActive() {
		return active;
	}

	public static SlotProperties.SlotData getSlotProperties(int slot) {
		return slotProperties.getData(slot);
	}

	public static void setSlotProperties(int slot, SlotProperties.SlotData data) {
		slotProperties.setData(slot, data);
		slotProperties.save(SimpleBlueprints.configPath().resolve("slots.properties"));
	}

	public static BlueprintTool currentTool() {
		return tool;
	}

	public static void setToolSlot(int i) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		player.getInventory().setSelectedSlot(i);
	}

	public static int getToolSlot() {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return 0;
		return player.getInventory().getSelectedSlot();
	}

	public static String getSlotName(int slot) {
		return selectionData[slot].name;
	}

	public static void setSlotName(int slot, String name) {
		selectionData[slot].name = name;
		selectionData[slot].markDirty();
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

		for (BlueprintData data : selectionData) {
			if (data.graphic != null && data.isVisible) {
				WorldRenderer.addGraphic(data.graphic);
			}
		}

		updateGraphics();
	}

	private static void disable() {
		active = false;
		HUD.setVisible(false);

		WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
		WorldRenderer.removeGraphic(SELECTION_OUTLINE);

		if (!showBlocks) {
			for (BlueprintData data : selectionData) {
				if (data.graphic != null) {
					WorldRenderer.removeGraphic(data.graphic);
				}
			}
		}

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

	public static BlueprintBlockData getData() {
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

	public static boolean isSlotEmpty(int slot) {
		return !hasSelectionForSlot(slot) && !hasDataInSlot(slot);
	}

	public static boolean hasPlacedData() {
		return selection.isActive && selection.data != null;
	}

	public static boolean hasAnyPlacedData() {
		for (BlueprintData selection : selectionData) {
			if (selection.isActive && selection.data != null) {
				return true;
			}
		}

		return false;
	}

	public static void setData(BlueprintBlockData data) {
		selection.data = data;
		selection.isVisible = true;

		if (data == null) {
			selection.name = "";
		}

		selection.markDirty();
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

		selection.markDirty();
		updateGraphics();
	}

	public static void clearSelection() {
		selection.isActive = false;
		selection.markDirty();
		updateGraphics();
	}

	public static void setBlockGraphic(BlueprintGraphic graphic) {
		if (selection.graphic != null) {
			WorldRenderer.removeGraphic(selection.graphic);
		}

		selection.graphic = graphic;
		graphic.setPosition(selection.min);

		if (showBlocks && selection.isVisible) {
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

		for (BlueprintData selection : selectionData) {
			if (selection.graphic != null) {
				selection.graphic.setAlpha(alpha);
			}
		}
	}

	public static void cycleBlockAlpha() {
		float alpha = BlueprintManager.getBlockAlpha();

		alpha -= ALPHA_STEP_SIZE;

		if (alpha < 0f) {
			alpha = 1f;
		}

		BlueprintManager.setBlockAlpha(alpha);
	}

	public static float getBlockAlpha() {
		return blockAlpha;
	}

	public static void setBlockColor(Color color) {
		blockColor = color;

		for (BlueprintData selection : selectionData) {
			if (selection.graphic != null) {
				selection.graphic.setTint(color);
			}
		}
	}

	public static void cycleBlockColor() {
		colorIndex++;

		if (colorIndex >= COLORS.length) {
			colorIndex = 0;
		}

		BlueprintManager.setBlockColor(COLORS[colorIndex]);
	}

	public static Color getBlockColor() {
		return blockColor;
	}

	public static boolean isVisible() {
		return selection.isVisible;
	}

	public static void setVisibility(boolean visible) {
		if (!hasData()) return;

		selection.isVisible = visible;

		if ((showBlocks || active) && visible) {
			selection.graphic.setPosition(selection.min);
			WorldRenderer.addGraphic(selection.graphic);
		}
		else {
			WorldRenderer.removeGraphic(selection.graphic);
		}

		selection.markDirty();
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

	public static ItemListTool.CountMode getBlockListMode() {
		return blockListMode;
	}

	public static void setBlockListMode(ItemListTool.CountMode mode) {
		blockListMode = mode;
	}

	public static void moveSelection(Direction direction, int steps) {
		if (!selection.isActive) return;

		selection.min = selection.min.relative(direction, steps);
		selection.max = selection.max.relative(direction, steps);

		selection.markDirty();
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

		selection.markDirty();
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

		selection.markDirty();
		updateGraphics();
	}

	public static void rotateSelection() {
		if (selection.data == null) return;

		selection.data.rotate();
		selection.graphic.setRotation(selection.data.getRotation());

		selection.max = selection.min.offset(selection.data.transformedSize()).offset(-1, -1, -1);

		selection.markDirty();
		updateGraphics();
	}

	public static void mirrorSelection(Direction.Axis axis) {
		if (selection.data == null) return;
		if (axis == Direction.Axis.Y) return;

		selection.data.mirror(axis);
		selection.graphic.setMirror(selection.data.getMirror());
		selection.graphic.setRotation(selection.data.getRotation());

		selection.max = selection.min.offset(selection.data.transformedSize()).offset(-1, -1, -1);

		selection.markDirty();
		updateGraphics();
	}

	public static void importFrom(Path file) {
		BlueprintData newData = new BlueprintData();
		newData.loadData(file);

		if (selection.graphic != null) {
			WorldRenderer.removeGraphic(selection.graphic);
		}

		selection = newData;
		selectionData[selectedSlot] = newData;

		setupData(selection);

		selection.markDirty();
		updateGraphics();
	}

	private static void setupData(BlueprintData data) {
		if (data.data == null) return;

		data.graphic = data.data.toGraphic(Minecraft.getInstance().level);
		data.graphic.setMirror(data.data.getMirror());
		data.graphic.setRotation(data.data.getRotation());
		data.graphic.setTint(blockColor);
		data.graphic.setAlpha(blockAlpha);

		if (showBlocks && data.isVisible) {
			data.graphic.setPosition(data.min);
			WorldRenderer.addGraphic(data.graphic);
		}
	}

	public static boolean exportTo(Path file, boolean asSchematic) {
		if (selection.data == null) {
			return false;
		}

		if (!selection.saveData(file, asSchematic)) {
			return false;
		}

		selection.markDirty();
		return true;
	}

	private static void updateGraphics() {
		if (!selection.isActive) {
			WorldRenderer.removeGraphic(SELECTION_GRAPHIC);
			WorldRenderer.removeGraphic(SELECTION_OUTLINE);

			if (selection.graphic != null) {
				WorldRenderer.removeGraphic(selection.graphic);
			}

			return;
		}

		if (selection.graphic != null && showBlocks && selection.isVisible) {
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
