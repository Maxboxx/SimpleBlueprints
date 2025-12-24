package maxboxx.blueprints;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
	public static final KeyMapping.Category main_category = create_category("keys");

	public static final KeyMapping TOGGLE = create_key("toggle", GLFW.GLFW_KEY_B, main_category);
	public static final KeyMapping VISIBILITY = create_key("visibility", GLFW.GLFW_KEY_UNKNOWN, main_category);

	private static KeyMapping.Category create_category(String name) {
		return KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, name));
	}

	private static KeyMapping create_key(String name, int keyCode, KeyMapping.Category category) {
		return new KeyMapping(SimpleBlueprints.MOD_ID + ".keys." + name, keyCode, category);
	}

	public static void init() {
		KeyBindingHelper.registerKeyBinding(TOGGLE);
		KeyBindingHelper.registerKeyBinding(VISIBILITY);
	}

	public static boolean consume(KeyMapping key) {
		boolean consumed = false;

		while (key.consumeClick()) {
			consumed = true;
		}

		key.setDown(false);
		return consumed;
	}
}
