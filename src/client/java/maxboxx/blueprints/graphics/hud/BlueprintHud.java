package maxboxx.blueprints.graphics.hud;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.tools.BlockListTool;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.ToolAction;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;

public class BlueprintHud extends HudGraphic {
	private static final Identifier LEFT_ICON   = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_left");
	private static final Identifier RIGHT_ICON  = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_right");
	private static final Identifier MIDDLE_ICON = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_middle");

	private static final int BLOCK_LIST_Y_OFFSET = 5;
	private static final int BLOCK_LIST_X_OFFSET = -20;
	private static final int BLOCK_OFFSET = 18;

	public BlueprintHud() {
		super("hud", VanillaHudElements.HOTBAR);
	}

	@Override
	public void render(GuiGraphics context) {
		BlueprintTool tool = BlueprintManager.currentTool();

		if (tool.isAvailable()) {
			tool.getAction(ToolAction.LEFT).ifPresent(action -> drawAction(context, 84, LEFT_ICON, action));
			tool.getAction(ToolAction.RIGHT).ifPresent(action -> drawAction(context, 72, RIGHT_ICON, action));
			tool.getAction(ToolAction.MIDDLE).ifPresent(action -> drawAction(context, 60, MIDDLE_ICON, action));
		}
		else {
			context.drawCenteredString(
				Minecraft.getInstance().font,
				SimpleBlueprints.text("tool.unavailable"),
				context.guiWidth() / 2,
				context.guiHeight() - 72,
				0xffff8888
			);
		}

		Font font = Minecraft.getInstance().font;

		if (BlueprintManager.hasSelection()) {
			BlockPos min = BlueprintManager.getSelectionMin();
			BlockPos max = BlueprintManager.getSelectionMax();
			Vec3i   size = BlueprintManager.getSelectionSize();

			Component minText  = SimpleBlueprints.text("bounds.min", min.getX(), min.getY(), min.getZ());
			Component maxText  = SimpleBlueprints.text("bounds.max", max.getX(), max.getY(), max.getZ());
			Component sizeText = SimpleBlueprints.text("bounds.size", size.getX(), size.getY(), size.getZ());

			int width = Math.max(Math.max(font.width(minText), font.width(maxText)), font.width(sizeText));
			context.fill(7, 7, 14 + width, 40, ARGB.color(64, 0, 0, 0));

			context.drawString(font, minText, 10, 10, 0xffffffff);
			context.drawString(font, maxText, 10, 20, 0xffffffff);
			context.drawString(font, sizeText, 10, 30, 0xffffffff);
		}

		if (BlueprintManager.hasData() && BlueprintManager.currentTool() instanceof BlockListTool) {
			BlueprintData data = BlueprintManager.getData();

			final int blockCount = data.getItems().size();
			final int maxRows = (context.guiHeight() - BLOCK_LIST_Y_OFFSET * 2) / BLOCK_OFFSET;
			final int cols = Math.ceilDiv(blockCount, maxRows);

			int row = 0;
			int col = cols - 1;

			for (BlueprintData.ItemData block : data.getItems()) {
				ItemStack stack = new ItemStack(block.item(), block.count());
				if (stack.getCount() <= 0) continue;

				context.renderItem(stack, context.guiWidth() + BLOCK_LIST_X_OFFSET - BLOCK_OFFSET * col, BLOCK_LIST_Y_OFFSET + row * BLOCK_OFFSET);

				context.pose().pushMatrix();
				context.pose().scale(0.5f);

				drawCountText(context, font, stack, col, row, BlueprintManager.getBlockListMode());

				context.pose().popMatrix();

				col--;

				if (col < 0) {
					col = cols - 1;
					row++;
				}
			}
		}
	}

	private void drawCountText(GuiGraphics context, Font font, ItemStack stack, int col, int row, BlockListTool.CountMode mode) {
		int textX = (context.guiWidth() + BLOCK_LIST_X_OFFSET + BLOCK_OFFSET - 2 - BLOCK_OFFSET * col) * 2;
		int textY = (BLOCK_LIST_Y_OFFSET + row * BLOCK_OFFSET + 12) * 2;

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

	private void drawRightString(GuiGraphics context, Font font, String text, int x, int y) {
		context.drawString(font, text, x - font.width(text), y, 0xffffffff);
	}

	private void drawAction(GuiGraphics context, int y, Identifier icon, Component text) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, icon, context.guiWidth() / 2 - 66, context.guiHeight() - y - 2, 12, 12);

		context.drawString(
			Minecraft.getInstance().font,
			text,
			context.guiWidth() / 2 - 50,
			context.guiHeight() - y,
			0xffffffff
		);
	}
}
