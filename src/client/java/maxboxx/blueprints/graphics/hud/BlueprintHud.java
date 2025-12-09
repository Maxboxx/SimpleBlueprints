package maxboxx.blueprints.graphics.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class BlueprintHud extends HudGraphic {
	public BlueprintHud() {
		super("hud", VanillaHudElements.HOTBAR);
	}

	@Override
	public void render(GuiGraphics context) {
		context.drawCenteredString(
			Minecraft.getInstance().font,
			"Blueprint Mode Active",
			context.guiWidth() / 2,
			context.guiHeight() - 50,
			0xffffffff
		);
	}
}
