package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderType;

public abstract class WorldGraphic {
	private final RenderPipeline PIPELINE;
	private final RenderType RENDER_TYPE;

	protected WorldGraphic(RenderPipeline pipeline) {
		PIPELINE = pipeline;
		RENDER_TYPE = null;
	}

	protected WorldGraphic(RenderType type) {
		RENDER_TYPE = type;
		PIPELINE = null;
	}

	public RenderPipeline pipeline() {
		return PIPELINE;
	}

	public RenderType renderType() {
		return RENDER_TYPE;
	}

	public abstract void render(WorldRenderer.Context context);
}
