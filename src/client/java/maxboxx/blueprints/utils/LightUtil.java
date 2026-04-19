package maxboxx.blueprints.utils;

public class LightUtil {
	public static final int FULL_SKY = pack(15, 0);

	public static int pack(int sky, int block) {
		return (sky << 20) | (block << 4);
	}
}
