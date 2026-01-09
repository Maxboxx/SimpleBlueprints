package maxboxx.blueprints.graphics.ui;

import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.tools.ItemListTool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemRenderer {
	private static final int ITEM_GRID_X_OFFSET = -20;
	private static final int ITEM_GRID_Y_OFFSET = 5;
	private static final int ITEM_GRID_BOTTOM_OFFSET = 26;
	private static final int ITEM_OFFSET = 18;

	public static void render(GuiGraphics context, Font font, BlueprintData.ItemData item, int x, int y, ItemListTool.CountMode mode) {
		context.renderItem(item.stack(), x, y);

		context.pose().pushMatrix();
		context.pose().scale(0.5f);

		drawCountText(context, font, item.stack(), x, y, mode);

		context.pose().popMatrix();

		if (!item.visible()) {
			context.fill(x, y, x + 16, y + 16, 0x44000000);
		}
	}

	public static void renderGrid(GuiGraphics context, List<BlueprintData.ItemData> items, ItemListTool.CountMode mode, boolean showHidden) {
		int visibleCount = 0;

		for (BlueprintData.ItemData item : items) {
			if (showHidden || item.visible()) {
				visibleCount++;
			}
		}

		final int maxRows = (context.guiHeight() - ITEM_GRID_Y_OFFSET - ITEM_GRID_BOTTOM_OFFSET) / ITEM_OFFSET;
		final int cols = Math.ceilDiv(visibleCount, maxRows);

		int row = 0;
		int col = cols - 1;

		Font font = Minecraft.getInstance().font;

		for (BlueprintData.ItemData data : items) {
			if (!showHidden && !data.visible()) continue;

			ItemStack stack = data.stack();
			if (stack.getCount() <= 0) continue;

			int x = context.guiWidth() + ITEM_GRID_X_OFFSET - ITEM_OFFSET * col;
			int y = ITEM_GRID_Y_OFFSET + row * ITEM_OFFSET;

			render(context, font, data, x, y, mode);

			col--;

			if (col < 0) {
				col = cols - 1;
				row++;
			}
		}
	}

	public static int getItemIndexAtPosition(int width, int height, List<BlueprintData.ItemData> items, int x, int y, boolean showHidden) {
		int visibleCount = 0;

		for (BlueprintData.ItemData item : items) {
			if (showHidden || item.visible()) {
				visibleCount++;
			}
		}

		final int maxRows = (height - ITEM_GRID_Y_OFFSET - ITEM_GRID_BOTTOM_OFFSET) / ITEM_OFFSET;
		final int cols = Math.ceilDiv(visibleCount, maxRows);

		int row = 0;
		int col = cols - 1;

		for (int i = 0; i < items.size(); i++) {
			if (!showHidden && !items.get(i).visible()) continue;
			if (items.get(i).stack().getCount() <= 0) continue;

			int x2 = width + ITEM_GRID_X_OFFSET - ITEM_OFFSET * col;
			int y2 = ITEM_GRID_Y_OFFSET + row * ITEM_OFFSET;

			if (x >= x2 && x <= x2 + 16 && y >= y2 && y <= y2 + 16) {
				return i;
			}

			col--;

			if (col < 0) {
				col = cols - 1;
				row++;
			}
		}

		return -1;
	}

	private static void drawCountText(GuiGraphics context, Font font, ItemStack stack, int x, int y, ItemListTool.CountMode mode) {
		int textX = (x + ITEM_OFFSET - 2) * 2;
		int textY = (y + 12) * 2;

		switch (mode) {
			case TOTAL -> drawRightString(context, font, String.valueOf(stack.getCount()), textX, textY);

			case STACKS -> {
				int stackSize = stack.getMaxStackSize();

				int stacks = stack.getCount() / stackSize;
				int items  = stack.getCount() - stacks * stackSize;

				if (stackSize <= 1) {
					stacks = 0;
					items = stack.getCount();
				}

				if (items > 0) {
					drawRightString(context, font, String.valueOf(items), textX, textY);
					textY -= 10;
				}

				if (stacks > 0) {
					drawRightString(context, font, stacks + "s", textX, textY);
				}
			}

			case CHESTS -> {
				int chestSize = 27;
				int stackSize = stack.getMaxStackSize();

				int stacks = stack.getCount() / stackSize;
				int chests = stacks / chestSize;
				int items  = stack.getCount() - stacks * stackSize;
				stacks -= chests * chestSize;

				if (stackSize <= 1) {
					items = stacks;
					stacks = 0;
				}

				if (items > 0) {
					drawRightString(context, font, String.valueOf(items), textX, textY);
					textY -= 10;
				}

				if (stacks > 0) {
					drawRightString(context, font, stacks + "s", textX, textY);
					textY -= 10;
				}

				if (chests > 0) {
					drawRightString(context, font, chests + "c", textX, textY);
				}
			}
		}
	}

	private static void drawRightString(GuiGraphics context, Font font, String text, int x, int y) {
		context.drawString(font, text, x - font.width(text), y, 0xffffffff);
	}
}
