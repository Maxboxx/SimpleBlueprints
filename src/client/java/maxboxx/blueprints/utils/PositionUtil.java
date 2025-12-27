package maxboxx.blueprints.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class PositionUtil {
	public static BlockPos rotateInBox(BlockPos pos, Vec3i size, Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90 -> new BlockPos(size.getZ() - pos.getZ() - 1, pos.getY(), pos.getX());
			case CLOCKWISE_180 -> new BlockPos(size.getX() - pos.getX() - 1, pos.getY(), size.getZ() - pos.getZ() - 1);
			case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), size.getX() - pos.getX() - 1);

			default -> pos;
		};
	}

	public static BlockPos mirrorInBox(BlockPos pos, Vec3i size, Mirror mirror) {
		return switch (mirror) {
			case FRONT_BACK -> new BlockPos(size.getX() - pos.getX() - 1, pos.getY(), pos.getZ());
			case LEFT_RIGHT -> new BlockPos(pos.getX(), pos.getY(), size.getZ() - pos.getZ() - 1);

			default -> pos;
		};
	}

	public static BlockPos mirrorAndRotateInBox(BlockPos pos, Vec3i size, Mirror mirror, Rotation rotation) {
		return rotateInBox(mirrorInBox(pos, size, mirror), size, rotation);
	}
}
