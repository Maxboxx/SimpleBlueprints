package maxboxx.blueprints.utils;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
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

	public static boolean isDoubleBlock(BlockState block) {
		if (block.getProperties().contains(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			return true;
		}

		if (block.getBlock() instanceof BedBlock) {
			return true;
		}

		return false;
	}

	public static boolean isPrimaryBlock(BlockState block) {
		if (block.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
			return block.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		}

		if (block.getBlock() instanceof BedBlock) {
			return block.getValue(BedBlock.PART) == BedPart.FOOT;
		}

		return true;
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

		CommandUtil.sendCommand(player, "setblock", position.getX(), position.getY(), position.getZ(), blockIdAndProperties(block));
	}

	public static void fillBlocks(LocalPlayer player, BlockPos min, BlockPos max, BlockState block) {
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = min.getY(); y <= max.getY(); y++) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					placeBlock(player, new BlockPos(x, y, z), block);
				}
			}
		}
	}

	private static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
		Property<?> key = entry.getKey();
		Comparable<?> value = entry.getValue();
		String valueName = Util.getPropertyName(key, value);
		return key.getName() + "=" + valueName;
	}
}
