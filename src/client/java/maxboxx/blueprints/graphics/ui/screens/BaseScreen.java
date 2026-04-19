package maxboxx.blueprints.graphics.ui.screens;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public abstract class BaseScreen extends Screen {

	protected BaseScreen(Component title) {
		super(title);
	}

	@Override
	public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
		if (getFocused() != null && !getFocused().isMouseOver(event.x(), event.y())) {
			clearFocus();
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == GLFW.GLFW_KEY_ENTER) {
			clearFocus();
		}

		return super.keyPressed(event);
	}
}
