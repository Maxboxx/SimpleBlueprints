package maxboxx.blueprints.graphics.ui.hud;

import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public abstract class HudGraphic {
	private final Identifier ID;
	private final Identifier TARGET;

	private boolean hidden = false;

	protected HudGraphic(String idName, Identifier target) {
		ID = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, idName);
		TARGET = target;
	}

	public Identifier id() {
		return ID;
	}

	public Identifier target() {
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
