package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import maxboxx.blueprints.data.Color;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintGraphic extends WorldGraphic implements BlockGetter {
	private final ArrayList<HashMap<BlockPos, BlockState>> blocks = new ArrayList<>();
	private final Level level;
	private final int minY;

	private final int sizeX, sizeZ;

	private final ArrayList<VertexCache> vertexLayers = new ArrayList<>();

	private int selectedLayer;
	private LayerMode layerMode = LayerMode.SHOW_ALL;

	private Rotation rotation = Rotation.NONE;
	private Mirror mirror = Mirror.NONE;

	private BlockPos offset;
	private Color color;
	private float alpha = 0.5f;

	public enum LayerMode {
		SHOW_ALL,
		SHOW_BELOW,
		SHOW_ABOVE,
		SHOW_SELECTED;

		public Component getText() {
			return switch (this) {
				case SHOW_ALL      -> Txt.key("layers.mode.show_all");
				case SHOW_BELOW    -> Txt.key("layers.mode.bottom");
				case SHOW_ABOVE    -> Txt.key("layers.mode.top");
				case SHOW_SELECTED -> Txt.key("layers.mode.slice");
			};
		}
	}

	public BlueprintGraphic(Level level, HashMap<BlockPos, BlockState> blocks) {
		super(WorldRenderer.TRANSLUCENT_BLOCKS);

		int minX = 100000;
		int maxX = -100000;

		int minY = 100000;
		int maxY = -100000;

		int minZ = 100000;
		int maxZ = -100000;

		for (BlockPos pos : blocks.keySet()) {
			if (pos.getX() < minX) minX = pos.getX();
			if (pos.getX() > maxX) maxX = pos.getX();
			if (pos.getY() < minY) minY = pos.getY();
			if (pos.getY() > maxY) maxY = pos.getY();
			if (pos.getZ() < minZ) minZ = pos.getZ();
			if (pos.getZ() > maxZ) maxZ = pos.getZ();
		}

		int height = maxY - minY + 1;

		sizeX = maxX - minX + 1;
		sizeZ = maxZ - minZ + 1;

		for (int i = 0; i < height; i++) {
			vertexLayers.add(new VertexCache());
			this.blocks.add(new HashMap<>());
		}

		for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
			int i = block.getKey().getY() - minY;
			this.blocks.get(i).put(block.getKey(), block.getValue());
		}

		selectedLayer = height - 1;
		this.minY = minY;
		this.level = level;
		this.offset = BlockPos.ZERO;

		this.color = Color.WHITE;
		layerMode = LayerMode.SHOW_BELOW;

		setupQuads();
	}

	public void setPosition(BlockPos offset) {
		this.offset = offset;
	}

	public void setTint(Color color) {
		this.color = color;

		for (VertexCache layer : vertexLayers) {
			layer.setColor(color.red(), color.green(), color.blue(), alpha);
		}
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;

		for (VertexCache layer : vertexLayers) {
			layer.setColor(color.red(), color.green(), color.blue(), alpha);
		}
	}

	public float getAlpha() {
		return alpha;
	}

	public int getSelectedLayer() {
		return selectedLayer;
	}

	public void setSelectedLayer(int layer) {
		if (layer < 0) {
			layer = 0;
		}
		else if (layer >= vertexLayers.size()) {
			layer = vertexLayers.size() - 1;
		}

		int diff = layer - selectedLayer;

		if (diff == 0) return;

		selectedLayer = layer;

		PoseStack stack = new PoseStack();
		RandomSource random = RandomSource.create(42L);

		BlockStateModelSet renderer = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

		if (diff < 0) {
			for (int i = selectedLayer - diff; i >= selectedLayer; i--) {
				setupLayer(stack, renderer, random, i);
			}
		}
		else {
			for (int i = selectedLayer - diff; i <= selectedLayer; i++) {
				setupLayer(stack, renderer, random, i);
			}
		}
	}

	public LayerMode getLayerMode() {
		return layerMode;
	}

	public void setLayerMode(LayerMode mode) {
		if (mode == layerMode) return;

		layerMode = mode;

		PoseStack stack = new PoseStack();
		RandomSource random = RandomSource.create(42L);
		BlockStateModelSet renderer = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

		if (selectedLayer > 0) {
			setupLayer(stack, renderer, random, selectedLayer - 1);
		}

		setupLayer(stack, renderer, random, selectedLayer);

		if (selectedLayer < vertexLayers.size() - 1) {
			setupLayer(stack, renderer, random, selectedLayer + 1);
		}
	}

	public void setRotation(Rotation rotation) {
		this.rotation = rotation;
	}

	public void setMirror(Mirror mirror) {
		this.mirror = mirror;
	}

	@Override
	public void render(WorldRenderer.Context context) {
		VertexConsumer consumer = context.builder();

		context.matrices().pushPose();

		switch (rotation) {
			case NONE -> {
				context.matrices().translate(offset.getX(), offset.getY(), offset.getZ());
			}

			case CLOCKWISE_90 -> {
				context.matrices().translate(offset.getX() + sizeZ, offset.getY(), offset.getZ());

				context.matrices().rotateAround(
					new Quaternionf(new AxisAngle4f((float)(Math.PI * -0.5f), 0f, 1f, 0f)),
					0f, 0f, 0f
				);
			}

			case CLOCKWISE_180 -> {
				context.matrices().translate(offset.getX() + sizeX, offset.getY(), offset.getZ() + sizeZ);

				context.matrices().rotateAround(
					new Quaternionf(new AxisAngle4f((float)Math.PI, 0f, 1f, 0f)),
					0f, 0f, 0f
				);
			}

			case COUNTERCLOCKWISE_90 -> {
				context.matrices().translate(offset.getX(), offset.getY(), offset.getZ() + sizeX);

				context.matrices().rotateAround(
					new Quaternionf(new AxisAngle4f((float)(Math.PI * 0.5f), 0f, 1f, 0f)),
					0f, 0f, 0f
				);
			}
		}

		switch (mirror) {
			case LEFT_RIGHT -> {
				context.matrices().translate(0f, 0f, sizeZ);
			}

			case FRONT_BACK -> {
				context.matrices().translate(sizeX, 0f, 0f);
			}
		}

		for (int i = 0; i < vertexLayers.size(); i++) {
			if (!isLayerVisible(i)) continue;

			vertexLayers.get(i).transferTo(consumer, context.matrices().last(), mirror);
		}

		context.matrices().popPose();
	}

	private void setupQuads() {
		BlockStateModelSet blockRenderer = Minecraft.getInstance().getModelManager().getBlockStateModelSet();

		RandomSource random = RandomSource.create(42L);
		PoseStack stack = new PoseStack();

		for (int i = 0; i < vertexLayers.size(); i++) {
			setupLayer(stack, blockRenderer, random, i);
		}
	}

	private void setupLayer(PoseStack stack, BlockStateModelSet blockRenderer, RandomSource random, int index) {
		VertexCache layer = vertexLayers.get(index);
		layer.clear();

		boolean isVisible = isLayerVisible(index);

		List<BlockStateModelPart> parts = new ArrayList<>();

		for (Map.Entry<BlockPos, BlockState> block : blocks.get(index).entrySet()) {
			BlockState state = block.getValue();

			boolean hasQuads = false;

			BlockStateModel model = blockRenderer.get(state);

			parts.clear();
			model.collectParts(random, parts);

			for (BlockStateModelPart blockModelPart : parts) {
				for (Direction direction : Direction.values()) {
					List<BakedQuad> list = blockModelPart.getQuads(direction);

					if (!list.isEmpty()) hasQuads = true;

					if (Block.shouldRenderFace(state, getBlockState(block.getKey().offset(direction.getUnitVec3i()), isVisible), direction)) {
						setupQuadList(stack, block.getKey(), layer, list);
					}
				}

				List<BakedQuad> list = blockModelPart.getQuads(null);

				if (!list.isEmpty()) {
					hasQuads = true;
					setupQuadList(stack, block.getKey(), layer, list);
				}
			}

			if (!hasQuads) {
				BlockPos pos = block.getKey();

				VoxelShape shape = block.getValue().getShape(this, pos.offset(offset));

				try (TextureAtlasSprite sprite = model.particleMaterial().sprite()) {
					shape.forAllBoxes((a, b, c, d, e, f) -> {
						ShapeVertexUtil.createBoxWithUV(
							stack, layer,
							(float)a + pos.getX() + 0.02f, (float)b + pos.getY() + 0.02f, (float)c + pos.getZ() + 0.02f,
							(float)d + pos.getX() - 0.02f, (float)e + pos.getY() - 0.02f, (float)f + pos.getZ() - 0.02f,
							color.red(), color.green(), color.blue(), alpha,
							sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1()
						);
					});
				}
			}
		}

		layer.setColor(color.red(), color.green(), color.blue(), alpha);
	}

	private void setupQuadList(PoseStack stack, BlockPos pos, VertexCache cache, List<BakedQuad> list) {
		QuadInstance instance = new QuadInstance();
		instance.setColor(0xffffffff);
		instance.setLightCoords(0xffffff);
		instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

		for (BakedQuad bakedQuad : list) {
			Direction dir = bakedQuad.direction();

			stack.pushPose();
			stack.translate(
				pos.getX() + dir.getStepX() * -0.005f,
				pos.getY() + dir.getStepY() * -0.005f,
				pos.getZ() + dir.getStepZ() * -0.005f
			);

			cache.putBakedQuad(stack.last(), bakedQuad, instance);

			stack.popPose();
		}
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(@NotNull BlockPos blockPos) {
		BlockState state = getBlockOrNull(blockPos);

		if (state == null) {
			return null;
		}

		if (state.getBlock() instanceof EntityBlock entityBlock) {
			return entityBlock.newBlockEntity(blockPos, state);
		}

		return null;
	}

	@Override
	public @NotNull BlockState getBlockState(@NotNull BlockPos blockPos) {
		BlockState state = getBlockOrNull(blockPos);

		if (state == null) {
			return Blocks.AIR.defaultBlockState();
		}

		return state;
	}

	public @NotNull BlockState getBlockState(BlockPos blockPos, boolean useLayerVisibility) {
		BlockState state = useLayerVisibility ? getBlockOrNull(blockPos) : getBlockOrNullRaw(blockPos);

		if (state == null) {
			return Blocks.AIR.defaultBlockState();
		}

		return state;
	}

	@Override
	public @NotNull FluidState getFluidState(@NotNull BlockPos blockPos) {
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

	private boolean isInVisibleLayer(BlockPos pos) {
		return isLayerVisible(pos.getY() - minY);
	}

	private boolean isLayerVisible(int layer) {
		return switch (layerMode) {
			case SHOW_ALL -> true;
			case SHOW_ABOVE -> layer >= selectedLayer;
			case SHOW_BELOW -> layer <= selectedLayer;
			case SHOW_SELECTED -> layer == selectedLayer;
		};
	}

	private BlockState getBlockOrNull(BlockPos pos) {
		if (!isInVisibleLayer(pos)) {
			return null;
		}

		return getBlockOrNullRaw(pos);
	}

	private BlockState getBlockOrNullRaw(BlockPos pos) {
		int i = pos.getY() - minY;

		if (i >= 0 && i < blocks.size()) {
			if (blocks.get(i).containsKey(pos)) {
				return blocks.get(i).get(pos);
			}
		}

		return null;
	}
}
