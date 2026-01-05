package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;

public class ShapeVertexUtil {
	public static void createBox(PoseStack stack, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
		Matrix4f pose = stack.last().pose();

		// Front face
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);

		// Back face
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);

		// Left face
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

		// Right face
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);

		// Top face
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

		// Bottom face
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
	}

	public static void createBoxWithUV(PoseStack stack, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a, float uMin, float vMin, float uMax, float vMax) {
		Matrix4f pose = stack.last().pose();

		// Front face
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(uMin, vMax);
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(uMax, vMin);
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(uMin, vMin);

		// Back face
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(uMin, vMax);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(uMin, vMin);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(uMax, vMin);

		// Left face
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(uMin, vMax);
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(uMax, vMin);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(uMin, vMin);

		// Right face
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(uMin, vMax);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(uMin, vMin);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(uMax ,vMin);

		// Top face
		consumer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setUv(uMin, vMin);
		consumer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setUv(uMax, vMin);
		consumer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setUv(uMin, vMax);

		// Bottom face
		consumer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setUv(uMin, vMax);
		consumer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setUv(uMax, vMax);
		consumer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setUv(uMax, vMin);
		consumer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setUv(uMin, vMin);
	}

	public static void createFace(PoseStack stack, VertexConsumer consumer, Direction side, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, float r, float g, float b, float a) {
		switch (side) {
			case UP -> {
				consumer.addVertex(stack.last(), minX, maxY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, maxY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, minZ).setColor(r, g, b, a);
			}

			case DOWN -> {
				consumer.addVertex(stack.last(), minX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, minY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, minY, maxZ).setColor(r, g, b, a);
			}

			case SOUTH -> {
				consumer.addVertex(stack.last(), minX, minY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, minY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, maxY, maxZ).setColor(r, g, b, a);
			}

			case NORTH -> {
				consumer.addVertex(stack.last(), maxX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, maxY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, minZ).setColor(r, g, b, a);
			}

			case WEST -> {
				consumer.addVertex(stack.last(), minX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, minY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, maxY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), minX, maxY, minZ).setColor(r, g, b, a);
			}

			case EAST -> {
				consumer.addVertex(stack.last(), maxX, minY, maxZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, minY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, minZ).setColor(r, g, b, a);
				consumer.addVertex(stack.last(), maxX, maxY, maxZ).setColor(r, g, b, a);
			}
		}
	}
}
