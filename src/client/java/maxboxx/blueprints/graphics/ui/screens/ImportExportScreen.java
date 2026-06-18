package maxboxx.blueprints.graphics.ui.screens;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.data.BlueprintData;
import maxboxx.blueprints.utils.FileUtil;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

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
		importButton = Button.builder(Txt.key("import.import"), b -> {
			FileUtil.openFileDialogAsync(Txt.key("import.select_import").getString(), BlueprintData.IMPORT_FILTERS, path -> {
				if (!path.endsWith(".dat") && !path.endsWith(".nbt")) {
					return;
				}

				BlueprintManager.importFrom(Path.of(path));
				Minecraft.getInstance().gui.setScreen(null);
			});
		}).width(COL_WIDTH).build();

		exportButton = Button.builder(Txt.key("import.export"), b -> {
			FileUtil.saveFileDialogAsync(Txt.key("import.select_export").getString(), BlueprintData.BLUEPRINT_FILTERS, path -> {
				if (!path.endsWith(".dat")) {
					if (path.isEmpty()) {
						return;
					}

					path = path + ".dat";
				}

				BlueprintManager.exportTo(Path.of(path), false);
				Minecraft.getInstance().gui.setScreen(null);
			});
		}).width(COL_WIDTH).build();

		exportButton.setTooltip(Tooltip.create(Txt.key("import.export_tooltip")));

		exportSchematicButton = Button.builder(Txt.key("import.export_schematic"), b -> {
			FileUtil.saveFileDialogAsync(Txt.key("import.select_export").getString(), BlueprintData.SCHEMATIC_FILTERS, path -> {
				if (!path.endsWith(".nbt")) {
					if (path.isEmpty()) {
						return;
					}

					path = path + ".nbt";
				}

				BlueprintManager.exportTo(Path.of(path), true);
				Minecraft.getInstance().gui.setScreen(null);
			});
		}).width(COL_WIDTH).build();

		exportSchematicButton.setTooltip(Tooltip.create(Txt.key("import.export_schematic_tooltip")));

		cancelButton = Button.builder(Txt.key("import.cancel"), b -> {
			Minecraft.getInstance().gui.setScreen(null);
		}).width(COL_WIDTH).build();

		addRenderableWidget(importButton);
		addRenderableWidget(exportButton);
		addRenderableWidget(exportSchematicButton);
		addRenderableWidget(cancelButton);
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
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

		super.extractBackground(context, mouseX, mouseY, delta);

		int selectedSlot = BlueprintManager.getBlueprintSlot() + 1;

		context.textWithWordWrap(this.font, Txt.key("import.import_desc", selectedSlot), originX - COL_WIDTH_GAP, originY - 100, COL_WIDTH, 0xffffffff);
		context.textWithWordWrap(this.font, Txt.key("import.export_desc", selectedSlot), originX + HALF_GAP, originY - 100, COL_WIDTH, 0xffffffff);

		if (BlueprintManager.hasData()) {
			context.textWithWordWrap(this.font, Txt.key("import.import_warning", selectedSlot), originX - COL_WIDTH_GAP, originY - 45, COL_WIDTH, 0xffff8888);
		}
		else {
			context.textWithWordWrap(this.font, Txt.key("import.export_warning", selectedSlot), originX + HALF_GAP, originY - 65, COL_WIDTH, 0xffff8888);
		}
	}
}
