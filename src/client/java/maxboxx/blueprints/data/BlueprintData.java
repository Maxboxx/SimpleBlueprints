package maxboxx.blueprints.data;

import maxboxx.blueprints.utils.BlockUtil;
import maxboxx.blueprints.graphics.world.BlueprintGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import maxboxx.blueprints.utils.PositionUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BlueprintData {
	private StructureTemplate structure;
	private HashSet<Block> blocks;
	private BlockPos pos;
	private final StructurePlaceSettings settings = new StructurePlaceSettings();

	public Vec3i size() {
		return structure.getSize();
	}

	public Vec3i transformedSize() {
		return structure.getSize(settings.getRotation());
	}

	public BlockPos position() {
		return pos;
	}

	public void loadFromWorld(Level level, BlockPos position, Vec3i size) {
		structure = new StructureTemplate();
		blocks    = new HashSet<>();
		pos       = position;

		for (int x = 0; x < size.getX(); x++) {
			for (int y = 0; y < size.getY(); y++) {
				for (int z = 0; z < size.getZ(); z++) {
					blocks.add(level.getBlockState(new BlockPos(position.offset(new Vec3i(x, y, z)))).getBlock());
				}
			}
		}

		structure.fillFromWorld(
			level,
			position,
			size,
			false,
			new ArrayList<>()
		);
	}

	public boolean canPlace(LocalPlayer player) {
		return structure != null && player.isCreative();
	}

	public void placeInWorld(LocalPlayer player, BlockPos position) {
		if (structure == null) return;
		if (!player.isCreative()) return;

		for (Block block : blocks) {
			List<StructureTemplate.StructureBlockInfo> blockInfos = structure.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), block);

			for (StructureTemplate.StructureBlockInfo info : blockInfos) {
				BlockUtil.placeBlock(
					player,
					PositionUtil.mirrorAndRotateInBox(info.pos(), size(), settings.getMirror(), settings.getRotation()).offset(position),
					info.state().mirror(settings.getMirror()).rotate(settings.getRotation())
				);
			}
		}
	}

	public BlueprintGraphic toGraphic(Level level) {
		HashMap<BlockPos, BlockState> blockMap = new HashMap<>();

		for (Block block : blocks) {
			List<StructureTemplate.StructureBlockInfo> blockInfos = structure.filterBlocks(pos, new StructurePlaceSettings(), block);

			for (StructureTemplate.StructureBlockInfo info : blockInfos) {
				if (info.state().isAir()) continue;

				blockMap.put(info.pos().subtract(pos), info.state());
			}
		}

		return new BlueprintGraphic(level, blockMap, WorldRenderer.FILLED_TEX);
	}

	public void rotate() {
		settings.setRotation(switch (settings.getRotation()) {
			case NONE -> Rotation.CLOCKWISE_90;
			case CLOCKWISE_90 -> Rotation.CLOCKWISE_180;
			case CLOCKWISE_180 -> Rotation.COUNTERCLOCKWISE_90;
			case COUNTERCLOCKWISE_90 -> Rotation.NONE;
		});
	}

	public Rotation getRotation() {
		return settings.getRotation();
	}

	public void mirror(Direction.Axis axis) {
		if (axis == Direction.Axis.Y) return;

		if (settings.getRotation() == Rotation.NONE || settings.getRotation() == Rotation.CLOCKWISE_180) {
			if (axis == Direction.Axis.X) {
				settings.setMirror(settings.getMirror() == Mirror.FRONT_BACK ? Mirror.NONE : Mirror.FRONT_BACK);
			}
			else {
				settings.setMirror(settings.getMirror() == Mirror.LEFT_RIGHT ? Mirror.NONE : Mirror.LEFT_RIGHT);
			}
		}
		else {
			if (axis == Direction.Axis.X) {
				settings.setMirror(settings.getMirror() == Mirror.LEFT_RIGHT ? Mirror.NONE : Mirror.LEFT_RIGHT);
			}
			else {
				settings.setMirror(settings.getMirror() == Mirror.FRONT_BACK ? Mirror.NONE : Mirror.FRONT_BACK);
			}
		}
	}

	public void setMirror(Mirror mirror) {
		settings.setMirror(mirror);
	}

	public Mirror getMirror() {
		return settings.getMirror();
	}
}
