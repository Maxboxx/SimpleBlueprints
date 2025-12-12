package maxboxx.blueprints.tools;

import java.util.ArrayList;

public class BlueprintTools {
	public static final BlueprintTool NONE = new NoneTool();

	private static final ArrayList<BlueprintTool> TOOLS = new ArrayList<>();

	public static void init() {
		TOOLS.add(new SelectTool());
		TOOLS.add(new MoveTool());
		TOOLS.add(new ResizeTool());
	}

	public static BlueprintTool get(int index) {
		if (index >= 0 && index < TOOLS.size()) {
			return TOOLS.get(index);
		}

		return NONE;
	}

	public static int indexOf(BlueprintTool tool) {
		return TOOLS.indexOf(tool);
	}
}
