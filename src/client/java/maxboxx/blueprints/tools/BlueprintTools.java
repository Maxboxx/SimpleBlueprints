package maxboxx.blueprints.tools;

import java.util.ArrayList;
import java.util.List;

public class BlueprintTools {
	public static final BlueprintTool NONE = new NoneTool();

	private static final ArrayList<BlueprintTool> TOOLS = new ArrayList<>();
	private static List<BlueprintTool> subTools = null;

	public static void init() {
		TOOLS.add(new SelectTool());
		TOOLS.add(new MoveTool());
		TOOLS.add(new RotateTool());
		TOOLS.add(new ResizeTool());
		TOOLS.add(new ClipboardTool());
		TOOLS.add(new VisibilityTool());
		TOOLS.add(new LayerTool());
		TOOLS.add(new SlotSelectTool());
	}

	public static BlueprintTool get(int index) {
		List<BlueprintTool> tools = subTools != null ? subTools : TOOLS;

		if (index >= 0 && index < tools.size()) {
			return tools.get(index);
		}

		return NONE;
	}

	public static int indexOf(BlueprintTool tool) {
		if (subTools != null) {
			return  subTools.indexOf(tool);
		}

		return TOOLS.indexOf(tool);
	}

	public static int indexOfSafe(BlueprintTool tool) {
		return Math.max(indexOf(tool), 0);
	}

	public static void setSubTools(List<BlueprintTool> tools) {
		subTools = tools;
	}

	public static void clearSubTools() {
		subTools = null;
	}
}
