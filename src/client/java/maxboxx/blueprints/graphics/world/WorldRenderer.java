package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.*;
import maxboxx.blueprints.SimpleBlueprints;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

import java.util.*;

public class WorldRenderer {
	public static final Pipeline FILLED_NO_DEPTH = new Pipeline(RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled_no_depth"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.withCull(true)
		.build()
	));

	public static final Pipeline FILLED_QUADS = new Pipeline(RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled2"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(true)
		.build()
	));

	private static final RenderPipeline TRANSLUCENT_BLOCKS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/block"))
		.withSampler("Sampler0")
		.withSampler("Sampler2")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withShaderDefine("ALPHA_CUTOUT", 0.1f)
		.build()
	);

	public static final Pipeline BLOCK_PIPELINE = new Pipeline(TRANSLUCENT_BLOCKS).setTexture(TextureAtlas.LOCATION_BLOCKS).useLightmap().useModelOffset();

	private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

	private static final HashSet<WorldGraphic> activeGraphics = new HashSet<>();

	public record Context(PoseStack matrices, BufferBuilder builder, LevelRenderContext context) {

	}

	public static void init() {
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(WorldRenderer::renderGraphics);
	}

	public static void cleanup() {
		ALLOCATOR.close();

		for (WorldGraphic graphic : activeGraphics) {
			graphic.cleanup();
		}

		activeGraphics.clear();
	}

	public static void addGraphic(WorldGraphic graphic) {
		activeGraphics.add(graphic);
	}

	public static void removeGraphic(WorldGraphic graphic) {
		activeGraphics.remove(graphic);
		graphic.cleanup();
	}

	private static void renderGraphics(LevelRenderContext context) {
		for (WorldGraphic graphic : activeGraphics) {
			renderGraphic(context, graphic);
		}
	}

	private static void renderGraphic(LevelRenderContext context, WorldGraphic graphic) {
		WorldGraphic.RenderMode mode = graphic.mode();

		if (mode == WorldGraphic.RenderMode.Render) {
			buildGraphic(context, graphic);
		}
		else if (mode == WorldGraphic.RenderMode.Skip) {
			return;
		}

		graphic.getPipeline().draw(context, graphic.cachedMeshData, graphic.origin());
	}

	private static void buildGraphic(LevelRenderContext context, WorldGraphic graphic) {
		PoseStack matrices = context.poseStack();
		BufferBuilder builder = new BufferBuilder(ALLOCATOR, graphic.getPipeline().getPipeline().getVertexFormatMode(), graphic.getPipeline().getPipeline().getVertexFormat());

		graphic.render(new Context(matrices, builder, context));
		graphic.generateMesh(builder);
	}
}
