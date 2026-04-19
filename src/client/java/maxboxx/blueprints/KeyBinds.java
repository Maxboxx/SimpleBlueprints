package maxboxx.blueprints;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
	public static final KeyMapping SETTINGS = create_key("settings", GLFW.GLFW_KEY_UNKNOWN, mainCategory);
	public static final KeyMapping LAYER_UP = create_key("layer_up", GLFW.GLFW_KEY_UNKNOWN, quickCategory);
	public static final KeyMapping LAYER_DOWN = create_key("layer_down", GLFW.GLFW_KEY_UNKNOWN, quickCategory);
	public static final KeyMapping CHANGE_ALPHA = create_key("change_alpha", GLFW.GLFW_KEY_UNKNOWN, quickCategory);
	public static final KeyMapping CHANGE_COLOR = create_key("change_color", GLFW.GLFW_KEY_UNKNOWN, quickCategory);
	public static final KeyMapping EDIT_ITEMS = create_key("edit_items", GLFW.GLFW_KEY_UNKNOWN, quickCategory);

	private static KeyMapping.Category create_category(String name) {
		return KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, name));
	}

	private static KeyMapping create_key(String name, int keyCode, KeyMapping.Category category) {
		return new KeyMapping(SimpleBlueprints.MOD_ID + ".keys." + name, keyCode, category);
	}

	public static void init() {
		KeyMappingHelper.registerKeyMapping(TOGGLE);
		KeyMappingHelper.registerKeyMapping(VISIBILITY);
		KeyMappingHelper.registerKeyMapping(SETTINGS);
		KeyMappingHelper.registerKeyMapping(LAYER_UP);
		KeyMappingHelper.registerKeyMapping(LAYER_DOWN);
		KeyMappingHelper.registerKeyMapping(CHANGE_ALPHA);
		KeyMappingHelper.registerKeyMapping(CHANGE_COLOR);
		KeyMappingHelper.registerKeyMapping(EDIT_ITEMS);
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
