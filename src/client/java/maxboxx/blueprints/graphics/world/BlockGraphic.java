package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockGraphic extends WorldGraphic implements BlockAndTintGetter {
	private BlockState block;
	private BlockPos pos;
	private Level level;

	public BlockGraphic(Level level, BlockState block, BlockPos pos, RenderPipeline pipeline) {
		super(pipeline);
		this.block = block;
		this.pos = pos;
		this.level = level;
	}

	@Override
	public void render(WorldRenderer.Context context) {
		context.matrices().translate(pos.getX() + 0.005f, pos.getY() + 0.005f, pos.getZ() + 0.005f);
		context.matrices().scale(0.99f, 0.99f, 0.99f);

		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

		/*blockRenderer.renderBlockAsEntity(
			block,
			context.matrices(),
			b -> context.builder(),
			15,
			0,
			this,
			pos
		);*/

		renderModel(
			context.matrices().last(),
			context.builder(),
			blockRenderer.getBlockModel(block),
			1f, 1f, 1f, 0.5f,
			15, 0
		);

		/*ModelBlockRenderer.renderModel(
			context.matrices().last(),
			context.builder(),
			blockRenderer.getBlockModel(block),
			0.2f, 1f, 1f,
			15, 1
		);*/
	}

	public static void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, BlockStateModel blockStateModel, float r, float g, float b, float a, int i, int j) {
		for (BlockModelPart blockModelPart : blockStateModel.collectParts(RandomSource.create(42L))) {
			for (Direction direction : Direction.values()) {
				renderQuadList(pose, vertexConsumer, r, g, b, a, blockModelPart.getQuads(direction), i, j);
			}

			renderQuadList(pose, vertexConsumer, r, g, b, a, blockModelPart.getQuads(null), i, j);
		}
	}

	private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float r, float g, float b, float a, List<BakedQuad> list, int i, int j) {
		for (BakedQuad bakedQuad : list) {
			vertexConsumer.putBulkData(pose, bakedQuad, r, g, b, a, i, j);
		}
	}

	@Override
	public float getShade(Direction direction, boolean bl) {
		return 0;
	}

	@Override
	public @NotNull LevelLightEngine getLightEngine() {
		return level.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
		return 1;
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(BlockPos blockPos) {
		if (pos.equals(blockPos)) {
			return block;
		}

		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public @NotNull FluidState getFluidState(BlockPos blockPos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getHeight() {
		return level.getHeight();
	}

	@Override
	public int getMinY() {
		return level.getMinY();
	}
}
