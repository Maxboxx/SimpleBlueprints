package maxboxx.blueprints.data;

import maxboxx.blueprints.BlockUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BlueprintData {
	private StructureTemplate structure;
	private HashSet<Block> blocks;

	public void loadFromWorld(Level level, BlockPos position, Vec3i size) {
		structure = new StructureTemplate();
		blocks    = new HashSet<>();

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

	public void placeInWorld(LocalPlayer player, BlockPos position) {
		if (structure == null) return;
		if (!player.isCreative()) return;

		for (Block block : blocks) {
			List<StructureTemplate.StructureBlockInfo> blockInfos = structure.filterBlocks(position, new StructurePlaceSettings(), block);

			for (StructureTemplate.StructureBlockInfo info : blockInfos) {
				BlockUtil.placeBlock(player, info.pos(), info.state());
			}
		}
	}
}
