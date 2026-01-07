package maxboxx.blueprints.graphics.ui.hud;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.graphics.ui.ItemRenderer;
import maxboxx.blueprints.graphics.ui.screens.ItemListScreen;
import maxboxx.blueprints.tools.ItemListTool;
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

public class BlueprintHud extends HudGraphic {
	private static final Identifier LEFT_ICON   = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_left");
	private static final Identifier RIGHT_ICON  = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_right");
	private static final Identifier MIDDLE_ICON = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/mouse_middle");

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

		if (BlueprintManager.hasData() && BlueprintManager.currentTool() instanceof ItemListTool && !(Minecraft.getInstance().screen instanceof ItemListScreen)) {
			BlueprintData data = BlueprintManager.getData();
			ItemRenderer.renderGrid(context, data.getItems(), BlueprintManager.getBlockListMode(), false);
		}
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
