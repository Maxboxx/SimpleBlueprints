package maxboxx.blueprints.data;

public record Color(float red, float green, float blue) {
	public static final Color WHITE = new Color(1f, 1f, 1f);
	public static final Color BLACK = new Color(0f, 0f, 0f);

	public static final Color RED   = new Color(1f, 0f, 0f);
	public static final Color GREEN = new Color(0f, 1f, 0f);
	public static final Color BLUE  = new Color(0f, 0f, 1f);

	public static final Color YELLOW  = new Color(1f, 1f, 0f);
	public static final Color CYAN    = new Color(0f, 1f, 1f);
	public static final Color MAGENTA = new Color(1f, 0f, 1f);
}
