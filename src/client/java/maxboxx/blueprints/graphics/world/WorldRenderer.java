package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import maxboxx.blueprints.SimpleBlueprints;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

public class WorldRenderer {
	public static final RenderPipeline FILLED_NO_DEPTH = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled_no_depth"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(true)
		.build()
	);

	public static final RenderPipeline FILLED = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withCull(true)
		.build()
	);

	public static final RenderPipeline FILLED_QUADS = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled2"))
		.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
		.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthWrite(false)
		.withCull(true)
		.build()
	);

	public static final RenderPipeline FILLED_TEX = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET)
		.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/filled_tex"))
		.withVertexFormat(DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS)
		.withSampler("Sampler0")
		.withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
		.withBlend(BlendFunction.TRANSLUCENT)
		.withDepthWrite(false)
		.withCull(true)
		.build()
	);

	public static final RenderType TRANSLUCENT_BLOCKS = Sheets.translucentBlockItemSheet();

	public static final RenderType TRANSLUCENT_BLOCKS_NO_DEPTH = RenderTypeUtil.createFromPipeline("translucent_block_no_depth",
		RenderPipelines.register(RenderPipeline.builder(PipelineUtil.convertToSnippet(TRANSLUCENT_BLOCKS.pipeline()))
			.withLocation(Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "pipeline/blocks_no_depth"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.build()
		),
		TextureAtlas.LOCATION_BLOCKS
	);

	private static final ByteBufferBuilder allocator = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);

	private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
	private static final Vector3f MODEL_OFFSET = new Vector3f();
	private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
	private static MappableRingBuffer vertexBuffer;

	private static final HashSet<WorldGraphic> activeGraphics = new HashSet<>();

	public record Context(PoseStack matrices, BufferBuilder builder, WorldRenderContext context) {

	}

	public static void init() {
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(WorldRenderer::renderGraphics);
	}

	public static void cleanup() {
		allocator.close();

		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}
	}

	public static void addGraphic(WorldGraphic graphic) {
		activeGraphics.add(graphic);
	}

	public static void removeGraphic(WorldGraphic graphic) {
		activeGraphics.remove(graphic);
	}

	private static void renderGraphics(WorldRenderContext context) {
		for (WorldGraphic graphic : activeGraphics) {
			renderGraphic(context, graphic);
		}
	}

	private static void renderGraphic(WorldRenderContext context, WorldGraphic graphic) {
		BufferBuilder builder = buildGraphic(context, graphic);
		drawGraphic(Minecraft.getInstance(), builder, graphic);
	}

	private static BufferBuilder buildGraphic(WorldRenderContext context, WorldGraphic graphic) {
		PoseStack matrices = context.matrices();
		Vec3 camera = context.worldState().cameraRenderState.pos;

		matrices.pushPose();
		matrices.translate(-camera.x, -camera.y, -camera.z);

		BufferBuilder builder;

		if (graphic.renderType() != null) {
			builder = new BufferBuilder(allocator, graphic.renderType().mode(), graphic.renderType().format());
		}
		else if (graphic.pipeline() != null) {
			builder = new BufferBuilder(allocator, graphic.pipeline().getVertexFormatMode(), graphic.pipeline().getVertexFormat());
		}
		else {
			builder = null;
		}

		graphic.render(new Context(matrices, builder, context));

		matrices.popPose();

		return builder;
	}

	private static void drawGraphic(Minecraft client, BufferBuilder builder, WorldGraphic graphic) {
		MeshData builtBuffer = builder.build();

		if (builtBuffer == null) {
			return;
		}

		MeshData.DrawState drawParameters = builtBuffer.drawState();
		VertexFormat format = drawParameters.format();

		GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

		if (graphic.renderType() != null) {
			graphic.renderType().draw(builtBuffer);
		}
		else if (graphic.pipeline() != null) {
			draw(client, graphic.pipeline(), builtBuffer, drawParameters, vertices, format);
		}

		vertexBuffer.rotate();
	}

	private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
		int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

		if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
			vertexBuffer = new MappableRingBuffer(() -> SimpleBlueprints.MOD_ID + " rendering", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
			MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
		}

		return vertexBuffer.currentBuffer();
	}

	private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
		GpuBuffer indices;
		VertexFormat.IndexType indexType;

		if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
			builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting());
			indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
			indexType = builtBuffer.drawState().indexType();
		} else {
			RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
			indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
			indexType = shapeIndexBuffer.type();
		}

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
			.writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

		try (RenderPass renderPass = RenderSystem.getDevice()
			.createCommandEncoder()
			.createRenderPass(() -> SimpleBlueprints.MOD_ID + " example render pipeline rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())
		) {
			renderPass.setPipeline(pipeline);

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);

			renderPass.setVertexBuffer(0, vertices);
			renderPass.setIndexBuffer(indices, indexType);

			renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
		}

		builtBuffer.close();
	}
}
