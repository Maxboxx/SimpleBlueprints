package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.*;
import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.Color;
import maxboxx.blueprints.data.Settings;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

import java.util.*;

public class WorldRenderer {
	public static final Pipeline FILLED_NO_DEPTH = new Pipeline(RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled_no_depth"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
		.withCull(true)
		.build()
	));

	public static final Pipeline FILLED_QUADS = new Pipeline(RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled2"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withCull(true)
		.build()
	));

	private static final RenderPipeline TRANSLUCENT_BLOCKS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.BLOCK_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/block"))
		//.withSampler("Sampler0")
		//.withSampler("Sampler2")
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withShaderDefine("ALPHA_CUTOUT", 0.1f)
		.build()
	);

	public static final Pipeline BLOCK_PIPELINE = new Pipeline(TRANSLUCENT_BLOCKS).setTexture(TextureAtlas.LOCATION_BLOCKS).useLightmap().useModelOffset();

	private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

	private static final HashSet<WorldGraphic> activeGraphics = new HashSet<>();
	private static final HashSet<WorldGraphic> lateGraphics = new HashSet<>();

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

		for (WorldGraphic graphic : lateGraphics) {
			graphic.cleanup();
		}

		lateGraphics.clear();
	}

	public static void addGraphic(WorldGraphic graphic) {
		activeGraphics.add(graphic);
	}

	public static void addLateGraphic(WorldGraphic graphic) {
		lateGraphics.add(graphic);
	}

	public static void removeGraphic(WorldGraphic graphic) {
		activeGraphics.remove(graphic);
		lateGraphics.remove(graphic);
	}

	private static void renderGraphics(LevelRenderContext context) {
		Frustum frustum = Minecraft.getInstance().gameRenderer.mainCamera().getCullFrustum();
		Vec3 camPos = Minecraft.getInstance().gameRenderer.mainCamera().position();

		for (WorldGraphic graphic : activeGraphics) {
			renderGraphic(context, graphic, frustum, camPos);
		}

		for (WorldGraphic graphic : lateGraphics) {
			renderGraphic(context, graphic, frustum, camPos);
		}
	}

	private static void renderGraphic(LevelRenderContext context, WorldGraphic graphic, Frustum frustum, Vec3 camPos) {
		AABB bounds = graphic.bounds();

		if (!frustum.isVisible(bounds)) {
			return;
		}

		int renderDist = Settings.RENDER_DISTANCE.getValue();

		if (renderDist > 0 && bounds.distanceToSqr(camPos) > renderDist * renderDist) {
			return;
		}

		WorldGraphic.RenderMode mode = graphic.mode();

		if (mode == WorldGraphic.RenderMode.Render) {
			buildGraphic(context, graphic);
		}
		else if (mode == WorldGraphic.RenderMode.Skip) {
			return;
		}

		Color c = BlueprintManager.getBlockColor();
		Vector4f color = new Vector4f(c.red(), c.green(), c.blue(), BlueprintManager.getBlockAlpha());
		graphic.getPipeline().draw(context, graphic, color);
	}

	private static void buildGraphic(LevelRenderContext context, WorldGraphic graphic) {
		PoseStack matrices = context.poseStack();
		BufferBuilder builder = new BufferBuilder(ALLOCATOR, graphic.getPipeline().getPipeline().getPrimitiveTopology(), graphic.getPipeline().getPipeline().getVertexFormatBinding(0));

		graphic.render(new Context(matrices, builder, context));
		graphic.generateMesh(builder);
	}
}
