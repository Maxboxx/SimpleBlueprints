package maxboxx.blueprints.data;

import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import java.nio.file.Path;

public class BlueprintData {
	public static final String[] FILE_FILTERS = new String[] {"*.dat"};

	public boolean isActive = false;
	public BlockPos min, max;

	public BlueprintBlockData data = null;
	public BlueprintGraphic graphic = null;

	public boolean isVisible = true;

	public String name = "";

	private boolean dirty = false;

	public boolean isDirty() {
		return dirty;
	}

	public void markDirty() {
		dirty = true;
	}

	public void saveData(Path file) {
		try {
			if (!isActive && data == null) {
				file.toFile().delete();
				dirty = false;
				return;
			}

			CompoundTag data = new CompoundTag();
			data.putBoolean("active", isActive);
			data.putBoolean("visible", isVisible);
			data.putString("name", name);

			if (isActive) {
				data.putIntArray("min", new int[] {min.getX(), min.getY(), min.getZ()});
				data.putIntArray("max", new int[] {max.getX(), max.getY(), max.getZ()});
			}

			if (this.data != null) {
				data.put("data", this.data.save());
			}

			NbtIo.writeCompressed(data, file);
		} catch (Exception e) {
			SimpleBlueprints.LOGGER.info("Failed to save blueprint data", e);
		}
	}

	public void loadData(Path file) {
		try {
			dirty = false;

			if (!file.toFile().exists()) {
				return;
			}

			CompoundTag data = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());

			isActive  = data.getBooleanOr("active", false);
			isVisible = data.getBooleanOr("visible", true);
			name      = data.getStringOr("name", "");

			data.getIntArray("min").ifPresent(values -> {
				if (values.length < 3) return;
				min = new BlockPos(values[0], values[1], values[2]);
			});

			data.getIntArray("max").ifPresent(values -> {
				if (values.length < 3) return;
				max = new BlockPos(values[0], values[1], values[2]);
			});

			Tag tag = data.get("data");

			if (tag instanceof CompoundTag nbt) {
				this.data = new BlueprintBlockData();
				this.data.load(nbt, min);
			}
		} catch (Exception e) {
			SimpleBlueprints.LOGGER.info("Failed to load blueprint data", e);
		}
	}
}
