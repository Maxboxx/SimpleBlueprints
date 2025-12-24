package maxboxx.blueprints.data;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import maxboxx.blueprints.BlockUtil;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BlockGraphic;
import maxboxx.blueprints.graphics.world.WorldRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

	public Vec3i size() {
		return structure.getSize();
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
			List<StructureTemplate.StructureBlockInfo> blockInfos = structure.filterBlocks(position, new StructurePlaceSettings(), block);

			for (StructureTemplate.StructureBlockInfo info : blockInfos) {
				BlockUtil.placeBlock(player, info.pos(), info.state());
			}
		}
	}

	public BlockGraphic toGraphic(Level level) {
		HashMap<BlockPos, BlockState> blockMap = new HashMap<>();

		for (Block block : blocks) {
			List<StructureTemplate.StructureBlockInfo> blockInfos = structure.filterBlocks(pos, new StructurePlaceSettings(), block);

			for (StructureTemplate.StructureBlockInfo info : blockInfos) {
				if (info.state().isAir()) continue;

				blockMap.put(info.pos().subtract(pos), info.state());
			}
		}

		return new BlockGraphic(level, blockMap, WorldRenderer.FILLED_TEX);
	}
}
