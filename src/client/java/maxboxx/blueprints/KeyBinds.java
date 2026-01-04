package maxboxx.blueprints;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeyBinds {
	public static final KeyMapping.Category mainCategory = create_category("keys");
	public static final KeyMapping.Category quickCategory = create_category("quick_keys");

	public static final KeyMapping TOGGLE = create_key("toggle", GLFW.GLFW_KEY_B, mainCategory);
	public static final KeyMapping VISIBILITY = create_key("visibility", GLFW.GLFW_KEY_UNKNOWN, mainCategory);
	public static final KeyMapping LAYER_UP = create_key("layer_up", GLFW.GLFW_KEY_UNKNOWN, quickCategory);
	public static final KeyMapping LAYER_DOWN = create_key("layer_down", GLFW.GLFW_KEY_UNKNOWN, quickCategory);

	private static KeyMapping.Category create_category(String name) {
		return KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, name));
	}

	private static KeyMapping create_key(String name, int keyCode, KeyMapping.Category category) {
		return new KeyMapping(SimpleBlueprints.MOD_ID + ".keys." + name, keyCode, category);
	}

	public static void init() {
		KeyBindingHelper.registerKeyBinding(TOGGLE);
		KeyBindingHelper.registerKeyBinding(VISIBILITY);
		KeyBindingHelper.registerKeyBinding(LAYER_UP);
		KeyBindingHelper.registerKeyBinding(LAYER_DOWN);
	}

	public static boolean consume(KeyMapping key) {
		boolean consumed = false;

		while (key.consumeClick()) {
			consumed = true;
		}

		key.setDown(false);
		return consumed;
	}

	public static boolean isShiftOrCtrlDown() {
		Options options = Minecraft.getInstance().options;
		return options.keyShift.isDown() || options.keySprint.isDown();
	}
}
