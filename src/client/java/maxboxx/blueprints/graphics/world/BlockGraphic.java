package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import maxboxx.blueprints.data.Color;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockGraphic extends WorldGraphic implements BlockAndTintGetter {
	private HashMap<BlockPos, BlockState> blocks;
	private Level level;

	private BlockPos offset;
	private Color color;
	private float alpha = 0.5f;

	public BlockGraphic(Level level, HashMap<BlockPos, BlockState> blocks, RenderPipeline pipeline) {
		super(pipeline);
		this.blocks = blocks;
		this.level = level;
		this.offset = BlockPos.ZERO;

		this.color = Color.WHITE;
	}

	public void setPosition(BlockPos offset) {
		this.offset = offset;
	}

	public void setTint(Color color) {
		this.color = color;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getAlpha() {
		return alpha;
	}

	@Override
	public void render(WorldRenderer.Context context) {
		BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

		for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
			context.matrices().pushPose();

			BlockPos pos = block.getKey().offset(offset);

			context.matrices().translate(pos.getX(), pos.getY(), pos.getZ());

			/*blockRenderer.renderBlockAsEntity(
				block.getValue(),
				context.matrices(),
				context.context().consumers(),
				0xffffff,
				0,
				this,
				pos
			);*/

			/*blockRenderer.renderBatched(
				block.getValue(),
				block.getKey(),
				this,
				context.matrices(),
				context.builder(),
				true,
				blockRenderer.getBlockModel(block.getValue()).collectParts(RandomSource.create())
			);*/

			renderModel(
				context.matrices().last(),
				block.getKey(),
				blockRenderer,
				RenderLayerHelper.entityDelegate(context.context().consumers()).getBuffer(ChunkSectionLayer.TRANSLUCENT),
				color.red(), color.green(), color.blue(), alpha,
				0xffffff, OverlayTexture.NO_OVERLAY
			);

			/*
			BlockState state = block.getValue();

			BlockEntity entity = this.getBlockEntity(block.getKey());

			if (entity != null) {
				BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);

				renderer.();
			}
			//*/

			/*
			blockRenderer.getBlockModelShaper().getModelManager().specialBlockModelRenderer().get().renderByBlock(
				state.getBlock(),
				ItemDisplayContext.NONE,
				context.matrices(),
				Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(),
				0xffffff,
				OverlayTexture.NO_OVERLAY, 0x00ff00
			);
			*/

			/*ModelBlockRenderer.renderModel(
				context.matrices().last(),
				context.builder(),
				blockRenderer.getBlockModel(block.getValue()),
				1f, 1f, 1f,
				15, 1
			);*/

			context.matrices().popPose();
		}
	}

	private void renderModel(PoseStack.Pose pose, BlockPos pos, BlockRenderDispatcher blockRenderer, VertexConsumer vertexConsumer, float r, float g, float b, float a, int i, int j) {
		BlockState state = blocks.get(pos);

		for (BlockModelPart blockModelPart : blockRenderer.getBlockModel(state).collectParts(RandomSource.create(42L))) {
			for (Direction direction : Direction.values()) {
				if (Block.shouldRenderFace(getBlockState(pos), getBlockState(pos.offset(direction.getUnitVec3i())), direction)) {
					renderQuadList(pose, vertexConsumer, r, g, b, a, blockModelPart.getQuads(direction), i, j);
				}
			}

			renderQuadList(pose, vertexConsumer, r, g, b, a, blockModelPart.getQuads(null), i, j);
		}
	}

	private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float r, float g, float b, float a, List<BakedQuad> list, int i, int j) {
		for (BakedQuad bakedQuad : list) {
			PoseStack.Pose dirPose = pose.copy();
			Direction dir = bakedQuad.direction();
			dirPose.translate(dir.getStepX() * -0.001f, dir.getStepY() * -0.001f, dir.getStepZ() * -0.001f);
			vertexConsumer.putBulkData(dirPose, bakedQuad, r, g, b, a, i, j);
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
		return level.getBlockTint(blockPos.offset(offset), colorResolver);
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos blockPos) {
		if (blocks.containsKey(blockPos)) {
			if (blocks.get(blockPos).getBlock() instanceof EntityBlock entityBlock) {
				return entityBlock.newBlockEntity(blockPos, blocks.get(blockPos));
			}
		}

		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(BlockPos blockPos) {
		if (blocks.containsKey(blockPos)) {
			return blocks.get(blockPos);
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
