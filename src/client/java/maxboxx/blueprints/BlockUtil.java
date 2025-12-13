package maxboxx.blueprints;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.io.IOException;
import java.util.Map;

public class BlockUtil {
	public static String blockId(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block).toString();
	}

	public static String blockIdAndProperties(BlockState block) {
		StringBuilder props = new StringBuilder();

		for (Map.Entry<Property<?>, Comparable<?>> entry : block.getValues().entrySet()) {
			if (props.isEmpty()) {
				props.append("[");
			}
			else {
				props.append(",");
			}

			props.append(getPropertyValueString(entry));
		}

		if (props.isEmpty()) {
			return blockId(block.getBlock());
		}

		props.append("]");
		return blockId(block.getBlock()) + props;
	}

	public static void placeBlock(LocalPlayer player, BlockPos position, BlockState block) {
		try (Level level = player.level()) {
			if (level.getBlockState(position).getBlock() == block.getBlock()) {
				return;
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		String posString = position.getX() + " " + position.getY() + " " + position.getZ();
		player.connection.sendCommand("setblock " + posString + " " + blockIdAndProperties(block));
	}

	private static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
		Property<?> key = entry.getKey();
		Comparable<?> value = entry.getValue();
		String valueName = Util.getPropertyName(key, value);
		return key.getName() + "=" + valueName;
	}
}
