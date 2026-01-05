package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Mirror;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;

public class VertexCache implements VertexConsumer {
	private int r, g, b, a;
	private final ArrayList<Vertex> vertices = new ArrayList<>();

	private record Vertex(
		float x, float y, float z,
		float u, float v,
		float nx, float ny, float nz
	) {
		public Vertex(float x, float y, float z) {
			this(x, y, z, 0f, 0f, 0f, 1f, 0f);
		}

		public Vertex withUv(float u, float v) {
			return new Vertex(x, y, z, u, v, nx, ny, nz);
		}

		public Vertex withNormal(float nx, float ny, float nz) {
			return new Vertex(x, y, z, u, v, nx, ny, nz);
		}
	}

	public void transferTo(VertexConsumer consumer, PoseStack.Pose pose, Mirror mirror) {
		Matrix4f mat = pose.pose();

		switch (mirror) {
			case NONE -> {
				for (Vertex vert : vertices) {
					consumer.addVertex(mat, vert.x, vert.y, vert.z);
					consumer.setColor(r, g, b, a);
					consumer.setUv(vert.u, vert.v);
					consumer.setLight(0xffffff);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setNormal(pose, vert.nx, vert.ny, vert.nz);
				}
			}

			case LEFT_RIGHT -> {
				for (Vertex vert : vertices.reversed()) {
					consumer.addVertex(mat, vert.x, vert.y, -vert.z);
					consumer.setColor(r, g, b, a);
					consumer.setUv(vert.u, vert.v);
					consumer.setLight(0xffffff);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setNormal(pose, vert.nx, vert.ny, -vert.nz);
				}
			}

			case FRONT_BACK -> {
				for (Vertex vert : vertices.reversed()) {
					consumer.addVertex(mat, -vert.x, vert.y, vert.z);
					consumer.setColor(r, g, b, a);
					consumer.setUv(vert.u, vert.v);
					consumer.setLight(0xffffff);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setNormal(pose, -vert.nx, vert.ny, vert.nz);
				}
			}
		}
	}

	public void clear() {
		vertices.clear();
	}

	@Override
	public @NotNull VertexConsumer addVertex(float f, float g, float h) {
		vertices.add(new Vertex(f, g, h));
		return this;
	}

	@Override
	public @NotNull VertexConsumer setColor(int i, int j, int k, int l) {
		r = i;
		g = j;
		b = k;
		a = l;
		return this;
	}

	@Override
	public @NotNull VertexConsumer setColor(int i) {
		r = ARGB.red(i);
		g = ARGB.green(i);
		b = ARGB.blue(i);
		a = ARGB.alpha(i);
		return this;
	}

	@Override
	public @NotNull VertexConsumer setUv(float f, float g) {
		vertices.set(vertices.size() - 1, vertices.getLast().withUv(f, g));
		return this;
	}

	@Override
	public @NotNull VertexConsumer setUv1(int i, int j) {
		return this;
	}

	@Override
	public @NotNull VertexConsumer setUv2(int i, int j) {
		return this;
	}

	@Override
	public @NotNull VertexConsumer setNormal(float f, float g, float h) {
		vertices.set(vertices.size() - 1, vertices.getLast().withNormal(f, g, h));
		return this;
	}

	@Override
	public @NotNull VertexConsumer setLineWidth(float f) {
		return this;
	}
}
