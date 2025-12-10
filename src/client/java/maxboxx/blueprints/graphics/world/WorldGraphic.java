package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;

public abstract class WorldGraphic {
	private final RenderPipeline PIPELINE;

	protected WorldGraphic(RenderPipeline pipeline) {
		PIPELINE = pipeline;
	}

	public RenderPipeline pipeline() {
		return PIPELINE;
	}

	public abstract void render(WorldRenderer.Context context);
}
