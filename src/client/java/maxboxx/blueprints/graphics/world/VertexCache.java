package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Mirror;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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

	public void transferTo(VertexConsumer consumer, PoseStack.Pose pose, Mirror mirror, boolean applyShade) {
		Matrix4f mat = pose.pose();

		switch (mirror) {
			case NONE -> {
				for (Vertex vert : vertices) {
					consumer.addVertex(mat, vert.x, vert.y, vert.z);

					if (applyShade) {
						Vector3f n = pose.transformNormal(vert.nx, vert.ny, vert.nz, new Vector3f());
						setColorWithShade(consumer, n.y, n.x);
					}
					else {
						consumer.setColor(r, g, b, a);
					}

					consumer.setUv(vert.u, vert.v);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setLight(0xf000f0);
					consumer.setNormal(pose, vert.nx, vert.ny, vert.nz);
				}
			}

			case LEFT_RIGHT -> {
				for (Vertex vert : vertices.reversed()) {
					consumer.addVertex(mat, vert.x, vert.y, -vert.z);

					if (applyShade) {
						Vector3f n = pose.transformNormal(vert.nx, vert.ny, vert.nz, new Vector3f());
						setColorWithShade(consumer, n.y, n.x);
					}
					else {
						consumer.setColor(r, g, b, a);
					}

					consumer.setUv(vert.u, vert.v);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setLight(0xf000f0);
					consumer.setNormal(pose, vert.nx, vert.ny, -vert.nz);
				}
			}

			case FRONT_BACK -> {
				for (Vertex vert : vertices.reversed()) {
					consumer.addVertex(mat, -vert.x, vert.y, vert.z);

					if (applyShade) {
						Vector3f n = pose.transformNormal(vert.nx, vert.ny, vert.nz, new Vector3f());
						setColorWithShade(consumer, n.y, n.x);
					}
					else {
						consumer.setColor(r, g, b, a);
					}

					consumer.setUv(vert.u, vert.v);
					consumer.setOverlay(OverlayTexture.NO_OVERLAY);
					consumer.setLight(0xf000f0);
					consumer.setNormal(pose, -vert.nx, vert.ny, vert.nz);
				}
			}
		}
	}

	private void setColorWithShade(VertexConsumer consumer, float ny, float nx) {
		if (ny > 0.5f) {
			consumer.setColor(r, g, b, a);
		}
		else if (ny < -0.5f) {
			consumer.setColor(Math.round((float)r * 0.5f), Math.round((float)g * 0.5f), Math.round((float)b * 0.5f), a);
		}
		else if (Math.abs(nx) > 0.5f) {
			consumer.setColor(Math.round((float)r * 0.6f), Math.round((float)g * 0.6f), Math.round((float)b * 0.6f), a);
		}
		else {
			consumer.setColor(Math.round((float)r * 0.8f), Math.round((float)g * 0.8f), Math.round((float)b * 0.8f), a);
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
