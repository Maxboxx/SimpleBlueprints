package maxboxx.blueprints.graphics.hud;

import maxboxx.blueprints.SimpleBlueprints;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class HudGraphic {
	private final ResourceLocation ID;
	private final ResourceLocation TARGET;

	private boolean hidden = false;

	protected HudGraphic(String idName, ResourceLocation target) {
		ID = ResourceLocation.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, idName);
		TARGET = target;
	}

	public ResourceLocation id() {
		return ID;
	}

	public ResourceLocation target() {
		return TARGET;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void show() {
		hidden = false;
	}

	public void hide() {
		hidden = true;
	}

	public void setVisible(boolean visible) {
		hidden = !visible;
	}

	public abstract void render(GuiGraphics context);
}
