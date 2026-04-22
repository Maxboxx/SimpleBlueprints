package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import maxboxx.blueprints.SimpleBlueprints;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public abstract class WorldGraphic {
	private final Pipeline pipeline;

	MappableRingBuffer vertexBuffer = null;
	int indexCount = 0;

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

	public abstract AABB bounds();

	public abstract RenderMode mode();

	public abstract void render(WorldRenderer.Context context);

	public void generateMesh(BufferBuilder buffer) {
		MeshData meshData = buffer.buildOrThrow();

		MeshData.DrawState drawState = meshData.drawState();
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

		try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, meshData.vertexBuffer().remaining()), false, true)) {
			MemoryUtil.memCopy(meshData.vertexBuffer(), mappedView.data());
		}

		indexCount = drawState.indexCount();

		meshData.close();
	}

	public void cleanup() {
		if (vertexBuffer != null) {
			vertexBuffer.close();
			vertexBuffer = null;
		}

		indexCount = 0;
	}
}
