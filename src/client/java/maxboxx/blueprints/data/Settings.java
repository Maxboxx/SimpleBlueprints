package maxboxx.blueprints.data;

import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

public class Settings {
	private static boolean isDirty = false;
	private static final List<Setting<?>> settings = new ArrayList<>();

	public static final IntSetting SLICE_LAYERS = new IntSetting("slice_layers",1, 1, 32);
	public static final IntSetting RENDER_DISTANCE = new IntSetting("render_distance",256, 0, 10000);
	public static final BooleanSetting DELETE_TOOL = new BooleanSetting("delete_tool", true);

	static {
		settings.add(SLICE_LAYERS);
		settings.add(RENDER_DISTANCE);
		settings.add(DELETE_TOOL);
	}

	public static Iterable<Setting<?>> getSettings() {
		return settings;
	}

	public static void load() {
		Properties props = new Properties();

		try {
			props.load(new FileInputStream(SimpleBlueprints.configFilePath("settings.properties").toFile()));
		}
		catch (IOException e) {
			SimpleBlueprints.LOGGER.error("Failed to load settings", e);
		}

		for (Setting<?> setting : settings) {
			setting.loadFrom(props);
		}

		isDirty = false;
	}

	public static void save() {
		if (!isDirty) return;

		Properties props = new Properties();

		for (Setting<?> setting : settings) {
			setting.saveTo(props);
		}

		try {
			props.store(new FileOutputStream(SimpleBlueprints.configFilePath("settings.properties").toFile()), "");
		}
		catch (IOException e) {
			SimpleBlueprints.LOGGER.error("Failed to save settings", e);
		}

		isDirty = false;
	}

	public static abstract class Setting<T> {
		public final String KEY;
		public final T DEFAULT;
		private @NotNull T value;

		private Set<BiConsumer<T, T>> consumers;

		public Setting(String key, @NonNull T defaultValue) {
			KEY = key;
			DEFAULT = defaultValue;
			value = defaultValue;
		}

		public Component name() {
			return Txt.key("setting." + KEY);
		}

		protected Component subText(String subKey) {
			return Txt.key("setting." + KEY + "." + subKey);
		}

		public void setValue(T value) {
			T prev = this.value;

			this.value = validate(value);
			isDirty = true;

			if (consumers != null) {
				for (BiConsumer<T, T> consumer : consumers) {
					consumer.accept(prev, this.value);
				}
			}
		}

		public @NotNull T getValue() {
			return value;
		}

		public void resetValue() {
			setValue(DEFAULT);
		}

		public void onValueChange(BiConsumer<T, T> change) {
			if (consumers == null) {
				consumers = new HashSet<>();
			}

			consumers.add(change);
		}

		protected abstract T validate(T value);
		protected abstract @Nullable T readValue(String value);
		protected abstract String writeValue(T value);

		private void loadFrom(Properties props) {
			String value = props.getProperty(KEY, "");

			if (value.isEmpty()) {
				resetValue();
				return;
			}

			T t = readValue(value);

			if (t == null) {
				resetValue();
				return;
			}

			setValue(readValue(value));
		}

		private void saveTo(Properties props) {
			props.setProperty(KEY, writeValue(getValue()));
		}
	}

	public static class IntSetting extends Setting<Integer> {
		public final Integer MIN, MAX;

		public IntSetting(String key, Integer defaultValue, Integer min, Integer max) {
			super(key, defaultValue);
			MIN = min;
			MAX = max;
		}

		@Override
		protected Integer validate(Integer value) {
			if (value < MIN) {
				return MIN;
			}
			else if (value > MAX) {
				return MAX;
			}
			else {
				return value;
			}
		}

		@Override
		protected @Nullable Integer readValue(String value) {
			try {
				return Integer.parseInt(value);
			}
			catch (NumberFormatException e) {
				return null;
			}
		}

		@Override
		protected String writeValue(Integer value) {
			return value.toString();
		}
	}

	public static class BooleanSetting extends Setting<Boolean> {
		public BooleanSetting(String key, Boolean defaultValue) {
			super(key, defaultValue);
		}

		public Component onText() {
			return subText("on");
		}

		public Component offText() {
			return subText("off");
		}

		public Component stateText() {
			return getValue() ? onText() : offText();
		}

		@Override
		protected Boolean validate(Boolean value) {
			return value;
		}

		@Override
		protected @Nullable Boolean readValue(String value) {
			try {
				return Objects.equals(value, "true");
			}
			catch (NumberFormatException e) {
				return null;
			}
		}

		@Override
		protected String writeValue(Boolean value) {
			return value ? "true" : "false";
		}
	}
}
