package maxboxx.blueprints.graphics.ui.screens;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.utils.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Locale;

public class ImportExportScreen extends Screen {
	private static final int COL_WIDTH = 120;
	private static final int COL_GAP = 30;
	private static final int HALF_GAP = COL_GAP / 2;
	private static final int COL_WIDTH_GAP = COL_WIDTH + HALF_GAP;

	private Button importButton;
	private Button exportButton;
	private Button exportSchematicButton;
	private Button cancelButton;

	public ImportExportScreen() {
		super(Component.empty());
	}

	@Override
	protected void init() {
		importButton = Button.builder(SimpleBlueprints.text("import.import"), b -> {
			FileUtil.openFileDialogAsync(SimpleBlueprints.text("import.select_import").getString(), BlueprintData.IMPORT_FILTERS, path -> {
				if (!hasExtension(path, ".dat") && !hasExtension(path, ".nbt")) {
					return;
				}

				BlueprintManager.importFrom(Path.of(path));
				Minecraft.getInstance().setScreen(null);
			});
		}).width(COL_WIDTH).build();

		exportButton = Button.builder(SimpleBlueprints.text("import.export"), b -> {
			FileUtil.saveFileDialogAsync(SimpleBlueprints.text("import.select_export").getString(), BlueprintData.BLUEPRINT_FILTERS, path -> {
				path = appendExtension(path, ".dat");
				if (path == null) {
					return;
				}

				if (BlueprintManager.exportTo(Path.of(path), false)) {
					Minecraft.getInstance().setScreen(null);
				}
			});
		}).width(COL_WIDTH).build();
		exportButton.setTooltip(Tooltip.create(SimpleBlueprints.text("import.export_tooltip")));

		exportSchematicButton = Button.builder(SimpleBlueprints.text("import.export_schematic"), b -> {
			FileUtil.saveFileDialogAsync(SimpleBlueprints.text("import.select_export").getString(), BlueprintData.SCHEMATIC_FILTERS, path -> {
				path = appendExtension(path, ".nbt");
				if (path == null) {
					return;
				}

				if (BlueprintManager.exportTo(Path.of(path), true)) {
					Minecraft.getInstance().setScreen(null);
				}
			});
		}).width(COL_WIDTH).build();
		exportSchematicButton.setTooltip(Tooltip.create(SimpleBlueprints.text("import.export_schematic_tooltip")));

		cancelButton = Button.builder(SimpleBlueprints.text("import.cancel"), b -> {
			Minecraft.getInstance().setScreen(null);
		}).width(COL_WIDTH).build();

		addRenderableWidget(importButton);
		addRenderableWidget(exportButton);
		addRenderableWidget(exportSchematicButton);
		addRenderableWidget(cancelButton);
	}

	@Override
	public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
		int originX = context.guiWidth() / 2;
		int originY = context.guiHeight() / 2 + 30;

		importButton.setPosition(originX - COL_WIDTH_GAP, originY);
		exportButton.setPosition(originX + HALF_GAP, originY - 25);
		exportSchematicButton.setPosition(originX + HALF_GAP, originY);
		cancelButton.setPosition(originX - COL_WIDTH / 2, originY + 40);

		exportButton.visible = BlueprintManager.hasData();
		exportSchematicButton.visible = BlueprintManager.hasData();

		context.fill(originX - COL_WIDTH_GAP - 5, originY - 105, originX - HALF_GAP + 5, originY + 25, 0x44000000);
		context.fill(originX + HALF_GAP - 5, originY - 105, originX + HALF_GAP + COL_WIDTH + 5, originY + 25, 0x44000000);

		super.render(context, mouseX, mouseY, delta);

		int selectedSlot = BlueprintManager.getBlueprintSlot() + 1;

		context.drawWordWrap(this.font, SimpleBlueprints.text("import.import_desc", selectedSlot), originX - COL_WIDTH_GAP, originY - 100, COL_WIDTH, 0xffffffff);
		context.drawWordWrap(this.font, SimpleBlueprints.text("import.export_desc", selectedSlot), originX + HALF_GAP, originY - 100, COL_WIDTH, 0xffffffff);

		if (BlueprintManager.hasData()) {
			context.drawWordWrap(this.font, SimpleBlueprints.text("import.import_warning", selectedSlot), originX - COL_WIDTH_GAP, originY - 45, COL_WIDTH, 0xffff8888);
		}
		else {
			context.drawWordWrap(this.font, SimpleBlueprints.text("import.export_warning", selectedSlot), originX + HALF_GAP, originY - 65, COL_WIDTH, 0xffff8888);
		}
	}

	private static boolean hasExtension(String path, String extension) {
		return path.toLowerCase(Locale.ROOT).endsWith(extension);
	}

	private static String appendExtension(String path, String extension) {
		if (path.isEmpty()) {
			return null;
		}
		return hasExtension(path, extension) ? path : path + extension;
	}
}
