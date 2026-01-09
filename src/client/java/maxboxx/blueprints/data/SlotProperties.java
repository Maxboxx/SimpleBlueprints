package maxboxx.blueprints.data;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class SlotProperties {
	private final SlotData[] slots = new SlotData[BlueprintManager.SLOT_COUNT];

	public record SlotData(String name, boolean persistent) {
		public boolean hasName() {
			return !name.isBlank();
		}

		public static SlotData empty() {
			return new SlotData("", true);
		}
	}

	public SlotProperties() {
		for (int i = 0; i < slots.length; i++) {
			slots[i] = SlotData.empty();
		}
	}

	public SlotData getData(int slot) {
		if (slot >= 0 && slot < slots.length) {
			return slots[slot];
		}

		return SlotData.empty();
	}

	public void setData(int slot, SlotData data) {
		if (slot >= 0 && slot < slots.length) {
			slots[slot] = data;
		}
	}

	public void load(Path path) {
		Properties props = new Properties();

		try {
			props.load(new FileInputStream(path.toFile()));
		}
		catch (IOException e) {
			for (int i = 0; i < slots.length; i++) {
				slots[i] = SlotData.empty();
			}

			SimpleBlueprints.LOGGER.error("Failed to load properties", e);
			return;
		}

		for (int i = 0; i < slots.length; i++) {
			slots[i] = new SlotData(
				props.getProperty("name" + (i + 1), ""),
				Objects.equals(props.getProperty("persistent" + (i + 1), "true"), "true")
			);
		}
	}

	public void save(Path path) {
		Properties props = new Properties();

		for (int i = 0; i < slots.length; i++) {
			props.setProperty("name" + (i + 1), slots[i].name);
			props.setProperty("persistent" + (i + 1), slots[i].persistent ? "true" : "false");
		}

		try {
			props.store(new FileOutputStream(path.toFile()), "");
		}
		catch (IOException e) {
			SimpleBlueprints.LOGGER.error("Failed to save properties", e);
			return;
		}
	}
}
