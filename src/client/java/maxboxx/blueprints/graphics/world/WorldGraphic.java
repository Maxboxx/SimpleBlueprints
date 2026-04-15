package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.renderer.MappableRingBuffer;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public abstract class WorldGraphic {
	private final Pipeline pipeline;

	public MeshData cachedMeshData = null;
	private MappableRingBuffer vertexBuffer = null;
	private GpuBuffer.MappedView cachedView;

	public enum RenderMode {
		Render,
		Reuse,
		Skip
	}

	protected WorldGraphic(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public abstract Vector3f origin();

	public abstract RenderMode mode();

	public abstract void render(WorldRenderer.Context context);

	public void generateMesh(BufferBuilder buffer) {
		if (cachedMeshData != null) cachedMeshData.close();
		cachedMeshData = buffer.buildOrThrow();

		MeshData.DrawState drawState = cachedMeshData.drawState();
		VertexFormat format = drawState.format();

		int vertexSize = drawState.vertexCount() * format.getVertexSize();

		if (vertexBuffer == null || vertexBuffer.size() < vertexSize) {
			if (vertexBuffer != null) vertexBuffer.close();
			vertexBuffer = new MappableRingBuffer(
				() -> SimpleBlueprints.MOD_ID + " persistent mesh",
				GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
				vertexSize
			);
		}

		CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

		if (cachedView != null) {
			cachedView.close();
			cachedView = null;
		}

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, cachedMeshData.vertexBuffer().remaining()), false, true)) {
			cachedView = mappedView;
			MemoryUtil.memCopy(cachedMeshData.vertexBuffer(), mappedView.data());
		}
	}

	public void cleanup() {
		if (cachedMeshData != null) {
			cachedMeshData.close();
			cachedMeshData = null;
		}

		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}

		if (cachedView != null) {
			cachedView.close();
			cachedView = null;
		}
	}
}
