package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import org.jetbrains.annotations.NotNull;

public class RotateResizeTool extends MultiTool {
	private final RotateTool rotateTool = new RotateTool();
	private final ResizeTool resizeTool = new ResizeTool();

	@Override
	protected @NotNull BlueprintTool getTool() {
		return BlueprintManager.hasData() ? rotateTool : resizeTool;
	}
}
