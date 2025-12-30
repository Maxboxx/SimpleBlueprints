package maxboxx.blueprints.data;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public record MirrorRotation(Mirror mirror, Rotation rotation) {
	public static final MirrorRotation NONE = new MirrorRotation(Mirror.NONE, Rotation.NONE);

	public MirrorRotation rotate() {
		return new MirrorRotation(mirror, rotation.getRotated(Rotation.CLOCKWISE_90));
	}

	public MirrorRotation rotateTo(Rotation rotation) {
		return new MirrorRotation(mirror, rotation);
	}

	public MirrorRotation mirrorTo(Mirror mirror) {
		return new MirrorRotation(mirror, rotation);
	}

	public MirrorRotation mirrorAxis(Direction.Axis axis) {
		if (axis == Direction.Axis.Y) return this;

		Direction.Axis rotatedAxis;

		if (rotation != Rotation.NONE && rotation != Rotation.CLOCKWISE_180) {
			rotatedAxis = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		}
		else {
			rotatedAxis = axis;
		}

		if (rotatedAxis == Direction.Axis.X) {
			return switch (mirror) {
				case NONE       -> new MirrorRotation(Mirror.FRONT_BACK, rotation);
				case FRONT_BACK -> new MirrorRotation(Mirror.NONE, rotation);
				case LEFT_RIGHT -> new MirrorRotation(Mirror.NONE, rotation.getRotated(Rotation.CLOCKWISE_180));
			};
		}
		else {
			return switch (mirror) {
				case NONE       -> new MirrorRotation(Mirror.LEFT_RIGHT, rotation);
				case LEFT_RIGHT -> new MirrorRotation(Mirror.NONE, rotation);
				case FRONT_BACK -> new MirrorRotation(Mirror.NONE, rotation.getRotated(Rotation.CLOCKWISE_180));
			};
		}
	}

	public int encode() {
		int data = switch (rotation) {
			case NONE -> 0;
			case CLOCKWISE_90 -> 1;
			case CLOCKWISE_180 -> 2;
			case COUNTERCLOCKWISE_90 -> 3;
		};

		data |= switch (mirror) {
			case NONE -> 0;
			case LEFT_RIGHT -> 1;
			case FRONT_BACK -> 2;
		} << 2;

		return data;
	}

	public static MirrorRotation decode(int data) {
		Rotation rot = switch (data & 3) {
			case 1 -> Rotation.CLOCKWISE_90;
			case 2 -> Rotation.CLOCKWISE_180;
			case 3 -> Rotation.COUNTERCLOCKWISE_90;

			default -> Rotation.NONE;
		};

		Mirror mirror = switch ((data >> 2) & 3) {
			case 1 -> Mirror.LEFT_RIGHT;
			case 2 -> Mirror.FRONT_BACK;

			default -> Mirror.NONE;
		};

		return new MirrorRotation(mirror, rot);
	}
}
