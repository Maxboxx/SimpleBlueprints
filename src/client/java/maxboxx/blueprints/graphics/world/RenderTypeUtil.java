package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class RenderTypeUtil {
	public static RenderType createfromPipeline(String name, RenderPipeline pipeline, Identifier texture) {
		RenderSetup renderSetup = RenderSetup.builder(pipeline)
			.withTexture("Sampler0", texture)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			.useOverlay()
			.affectsCrumbling()
			.sortOnUpload()
			.setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
			.createRenderSetup();
		return RenderType.create(name, renderSetup);
	}
}
