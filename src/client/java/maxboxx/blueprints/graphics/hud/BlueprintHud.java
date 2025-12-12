package maxboxx.blueprints.graphics.hud;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.BlueprintTools;
import maxboxx.blueprints.tools.ToolAction;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class BlueprintHud extends HudGraphic {
	public BlueprintHud() {
		super("hud", VanillaHudElements.HOTBAR);
	}

	@Override
	public void render(GuiGraphics context) {
		BlueprintTool tool = BlueprintManager.currentTool();

		tool.getAction(ToolAction.LEFT).ifPresent(action -> drawAction(context, 84, action));
		tool.getAction(ToolAction.RIGHT).ifPresent(action -> drawAction(context, 72, action));
		tool.getAction(ToolAction.MIDDLE).ifPresent(action -> drawAction(context, 60, action));
	}

	private void drawAction(GuiGraphics context, int y, Component text) {
		context.drawString(
			Minecraft.getInstance().font,
			text,
			context.guiWidth() / 2 - 50,
			context.guiHeight() - y,
			0xffffffff
		);
	}
}
