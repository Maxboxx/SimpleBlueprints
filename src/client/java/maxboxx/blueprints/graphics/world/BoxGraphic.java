package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.ShapeRenderer;

public class BoxGraphic extends WorldGraphic {
	public float minX, maxX, minY, maxY, minZ, maxZ;
	public float red, green, blue, alpha;
	private final boolean OUTLINE;

	private static final float LINE_OFFSET = 0.01f;
	private static final float OFFSET = 0.001f;

	public BoxGraphic(RenderPipeline pipeline, boolean outline) {
		super(pipeline);
		OUTLINE = outline;
	}

	@Override
	public void render(WorldRenderer.Context context) {
		if (OUTLINE) {
			renderLine(context, minX, minY, minZ, maxX, minY, minZ);
			renderLine(context, minX, maxY, minZ, maxX, maxY, minZ);
			renderLine(context, minX, minY, maxZ, maxX, minY, maxZ);
			renderLine(context, minX, maxY, maxZ, maxX, maxY, maxZ);

			renderLine(context, minX, minY, minZ, minX, maxY, minZ);
			renderLine(context, minX, minY, maxZ, minX, maxY, maxZ);
			renderLine(context, maxX, minY, minZ, maxX, maxY, minZ);
			renderLine(context, maxX, minY, maxZ, maxX, maxY, maxZ);

			renderLine(context, minX, minY, minZ, minX, minY, maxZ);
			renderLine(context, maxX, minY, minZ, maxX, minY, maxZ);
			renderLine(context, minX, maxY, minZ, minX, maxY, maxZ);
			renderLine(context, maxX, maxY, minZ, maxX, maxY, maxZ);
		}
		else {
			ShapeRenderer.addChainedFilledBoxVertices(
				context.matrices(), context.builder(),
				minX, minY, minZ,
				maxX, maxY, maxZ,
				red, green, blue, alpha
			);
		}
	}

	private void renderLine(WorldRenderer.Context context, float x1, float y1, float z1, float x2, float y2, float z2) {
		ShapeRenderer.addChainedFilledBoxVertices(
			context.matrices(), context.builder(),
			x1 - LINE_OFFSET, y1 - LINE_OFFSET, z1 - LINE_OFFSET,
			x2 + LINE_OFFSET, y2 + LINE_OFFSET, z2 + LINE_OFFSET,
			red, green, blue, alpha
		);
	}
}
