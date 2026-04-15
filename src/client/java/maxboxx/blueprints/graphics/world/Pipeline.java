package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class Pipeline {
	private final RenderPipeline pipeline;
	private OutputTarget target;

	private Identifier texture;
	private boolean hasLightmap;
	private boolean hasModelOffset;

	public Pipeline(RenderPipeline pipeline) {
		this.pipeline = pipeline;
		this.target = OutputTarget.MAIN_TARGET;
		this.texture = null;
		this.hasLightmap = false;
		this.hasModelOffset = false;
	}

	public Pipeline setTarget(OutputTarget target) {
		this.target = target;
		return this;
	}

	public Pipeline setTexture(Identifier texture) {
		this.texture = texture;
		return this;
	}

	public Pipeline useLightmap() {
		this.hasLightmap = true;
		return this;
	}

	public Pipeline useModelOffset() {
		this.hasModelOffset = true;
		return this;
	}

	public RenderPipeline getPipeline() {
		return pipeline;
	}

	public void draw(LevelRenderContext context, MeshData mesh, Vector3f origin) {
		if (mesh == null) return;

		Vec3 pos = context.levelState().cameraRenderState.pos;
		Vector3f offset = new Vector3f(-(float)pos.x, -(float)pos.y, -(float)pos.z).add(origin);

		if (!hasModelOffset) {
			RenderSystem.getModelViewStack().pushMatrix().translate(offset);
			offset = new Vector3f();
		}

		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
			RenderSystem.getModelViewMatrix(),
			new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
			offset,
			new Matrix4f()
		);

		GpuBuffer vertices = this.pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
		GpuBuffer indices;
		VertexFormat.IndexType indexType;

		if (mesh.indexBuffer() == null) {
			RenderSystem.AutoStorageIndexBuffer autoIndices = RenderSystem.getSequentialBuffer(mesh.drawState().mode());
			indices = autoIndices.getBuffer(mesh.drawState().indexCount());
			indexType = autoIndices.type();
		}
		else {
			indices = this.pipeline.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
			indexType = mesh.drawState().indexType();
		}

		RenderTarget renderTarget = this.target.getRenderTarget();
		GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : renderTarget.getColorTextureView();
		GpuTextureView depthTexture = renderTarget.useDepth ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView()) : null;

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Pipeline Draw", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
			renderPass.setPipeline(this.pipeline);

			ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();

			if (scissorState.enabled()) {
				renderPass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
			}

			if (this.texture != null) {
				AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(this.texture);

				renderPass.bindTexture(
					"Sampler0",
					tex.getTextureView(),
					tex.getSampler()
				);
			}

			if (hasLightmap) {
				renderPass.bindTexture(
					"Sampler2",
					Minecraft.getInstance().gameRenderer.lightmap(),
					RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
				);
			}

			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setUniform("DynamicTransforms", dynamicTransforms);
			renderPass.setVertexBuffer(0, vertices);

			renderPass.setIndexBuffer(indices, indexType);
			renderPass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
		}

		if (!hasModelOffset) {
			RenderSystem.getModelViewStack().popMatrix();
		}
	}
}
