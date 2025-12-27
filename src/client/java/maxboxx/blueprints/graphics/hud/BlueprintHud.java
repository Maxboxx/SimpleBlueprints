package maxboxx.blueprints.graphics.hud;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.SimpleBlueprintsClient;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.BlueprintTools;
import maxboxx.blueprints.tools.ToolAction;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

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
