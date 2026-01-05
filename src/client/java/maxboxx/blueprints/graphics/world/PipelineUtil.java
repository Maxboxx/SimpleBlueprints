package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;

import java.util.Optional;

public class PipelineUtil {
	public static RenderPipeline translucentBlock() {
		return Sheets.translucentBlockItemSheet().pipeline();
	}

	public static RenderPipeline.Snippet convertToSnippet(RenderPipeline pipeline) {
		RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.TERRAIN_SNIPPET);
		builder.withDepthWrite(pipeline.isWriteDepth());
		builder.withCull(pipeline.isCull());
		builder.withDepthTestFunction(pipeline.getDepthTestFunction());
		builder.withColorWrite(pipeline.isWriteColor(), pipeline.isWriteAlpha());
		builder.withDepthBias(pipeline.getDepthBiasScaleFactor(), pipeline.getDepthBiasConstant());
		builder.withFragmentShader(pipeline.getFragmentShader());
		builder.withLocation(pipeline.getLocation());
		builder.withPolygonMode(pipeline.getPolygonMode());
		builder.withVertexFormat(pipeline.getVertexFormat(), pipeline.getVertexFormatMode());
		builder.withVertexShader(pipeline.getVertexShader());

		for (RenderPipeline.UniformDescription uniform : pipeline.getUniforms()) {
			if (uniform.textureFormat() != null) {
				builder.withUniform(uniform.name(), uniform.type(), uniform.textureFormat());
			}
			else {
				builder.withUniform(uniform.name(), uniform.type());
			}
		}

		for (String sampler : pipeline.getSamplers()) {
			builder.withSampler(sampler);
		}

		Optional<BlendFunction> blend = pipeline.getBlendFunction();

		if (blend.isPresent()) {
			builder.withBlend(blend.get());
		}
		else {
			builder.withoutBlend();
		}

		return builder.buildSnippet();
	}
}
