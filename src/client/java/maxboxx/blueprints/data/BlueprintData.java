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
	private MirrorRotation mirrorRotation = MirrorRotation.NONE;

	public Vec3i size() {
		return structure.getSize();
	}

	public Vec3i halfSize() {
		Vec3i size = structure.getSize();
		return new Vec3i(size.getX() / 2, size.getY() / 2, size.getZ() / 2);
	}

	public Vec3i transformedSize() {
		return structure.getSize(mirrorRotation.rotation());
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
					PositionUtil.mirrorAndRotateInBox(info.pos(), size(), mirrorRotation.mirror(), mirrorRotation.rotation()).offset(position),
					info.state().mirror(mirrorRotation.mirror()).rotate(mirrorRotation.rotation())
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
		mirrorRotation = mirrorRotation.rotate();
	}

	public Rotation getRotation() {
		return mirrorRotation.rotation();
	}

	public void mirror(Direction.Axis axis) {
		mirrorRotation = mirrorRotation.mirrorAxis(axis);
	}

	public void setMirror(Mirror mirror) {
		mirrorRotation = mirrorRotation.mirrorTo(mirror);
	}

	public Mirror getMirror() {
		return mirrorRotation.mirror();
	}
}
