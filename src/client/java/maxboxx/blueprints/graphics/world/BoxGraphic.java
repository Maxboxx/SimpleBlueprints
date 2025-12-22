package maxboxx.blueprints.graphics.world;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.BufferBuilder;
import maxboxx.blueprints.data.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BoxGraphic extends WorldGraphic {
	public float minX, maxX, minY, maxY, minZ, maxZ;
	public Color color, color2;
	public float alpha;
	public Mode mode = Mode.NONE;
	private final boolean OUTLINE;

	private static final float LINE_OFFSET = 0.01f;

	public enum Mode {
		NONE,
		AXIS,
		CLOSE_FACE,
		FAR_FACE
	}

	public BoxGraphic(RenderPipeline pipeline, boolean outline) {
		super(pipeline);
		OUTLINE = outline;
	}

	public void setMin(float x, float y, float z) {
		minX = x;
		minY = y;
		minZ = z;
	}

	public void setMax(float x, float y, float z) {
		maxX = x;
		maxY = y;
		maxZ = z;
	}

	@Override
	public void render(WorldRenderer.Context context) {
		if (OUTLINE) {
			renderLine(context, color, minX, minY, minZ, maxX, minY, minZ);
			renderLine(context, color, minX, maxY, minZ, maxX, maxY, minZ);
			renderLine(context, color, minX, minY, maxZ, maxX, minY, maxZ);
			renderLine(context, color, minX, maxY, maxZ, maxX, maxY, maxZ);

			renderLine(context, color, minX, minY, minZ, minX, maxY, minZ);
			renderLine(context, color, minX, minY, maxZ, minX, maxY, maxZ);
			renderLine(context, color, maxX, minY, minZ, maxX, maxY, minZ);
			renderLine(context, color, maxX, minY, maxZ, maxX, maxY, maxZ);

			renderLine(context, color, minX, minY, minZ, minX, minY, maxZ);
			renderLine(context, color, maxX, minY, minZ, maxX, minY, maxZ);
			renderLine(context, color, minX, maxY, minZ, minX, maxY, maxZ);
			renderLine(context, color, maxX, maxY, minZ, maxX, maxY, maxZ);

			if (mode == Mode.AXIS) {
				LocalPlayer player = Minecraft.getInstance().player;

				if (player != null) {
					Direction dir = player.getNearestViewDirection();

					switch (dir.getAxis()) {
						case X -> {
							renderLine(
								context, color2,
								minX - 0.5f, (minY + maxY) * 0.5f - 0.01f, (minZ + maxZ) * 0.5f - 0.01f,
								maxX + 0.5f, (minY + maxY) * 0.5f + 0.01f, (minZ + maxZ) * 0.5f + 0.01f
							);
						}

						case Y -> {
							renderLine(
								context, color2,
								(minX + maxX) * 0.5f - 0.01f, minY - 0.5f, (minZ + maxZ) * 0.5f - 0.01f,
								(minX + maxX) * 0.5f + 0.01f, maxY + 0.5f, (minZ + maxZ) * 0.5f + 0.01f
							);
						}

						case Z -> {
							renderLine(
								context, color2,
								(minX + maxX) * 0.5f - 0.01f, (minY + maxY) * 0.5f - 0.01f, minZ - 0.5f,
								(minX + maxX) * 0.5f + 0.01f, (minY + maxY) * 0.5f + 0.01f, maxZ + 0.5f
							);
						}
					}
				}
			}
		}
		else {
			renderBox(context, true);
			renderBox(context, false);
		}
	}

	private void renderLine(WorldRenderer.Context context, Color color, float x1, float y1, float z1, float x2, float y2, float z2) {
		ShapeRenderer.addChainedFilledBoxVertices(
			context.matrices(), context.builder(),
			x1 - LINE_OFFSET, y1 - LINE_OFFSET, z1 - LINE_OFFSET,
			x2 + LINE_OFFSET, y2 + LINE_OFFSET, z2 + LINE_OFFSET,
			color.red(), color.green(), color.blue(), alpha
		);
	}

	private void renderBox(WorldRenderer.Context context, boolean inverted) {
		renderFace(context, Direction.UP, inverted, minX, maxY, minZ, maxX, maxY, maxZ);
		renderFace(context, Direction.DOWN, inverted, minX, minY, minZ, maxX, minY, maxZ);
		renderFace(context, Direction.EAST, inverted, maxX, minY, minZ, maxX, maxY, maxZ);
		renderFace(context, Direction.WEST, inverted, minX, minY, minZ, minX, maxY, maxZ);
		renderFace(context, Direction.NORTH, inverted, minX, minY, minZ, maxX, maxY, minZ);
		renderFace(context, Direction.SOUTH, inverted, minX, minY, maxZ, maxX, maxY, maxZ);
	}

	private void renderFace(WorldRenderer.Context context, Direction direction, boolean inverted, float x1, float y1, float z1, float x2, float y2, float z2) {
		boolean useColor2 = switch (mode) {
			case CLOSE_FACE -> {
				LocalPlayer player = Minecraft.getInstance().player;

				if (player == null) {
					yield false;
				}

				yield player.getNearestViewDirection() == direction.getOpposite();
			}

			case FAR_FACE -> {
				LocalPlayer player = Minecraft.getInstance().player;

				if (player == null) {
					yield false;
				}

				yield player.getNearestViewDirection() == direction;
			}

			default -> false;
		};

		Color color = useColor2 ? this.color2 : this.color;

		ShapeRenderer.renderFace(
			context.matrices().last().pose(), context.builder(), inverted ? direction.getOpposite() : direction,
			x1, y1, z1,
			x2, y2, z2,
			color.red(), color.green(), color.blue(), inverted ? alpha * 0.5f : alpha
		);
	}
}
