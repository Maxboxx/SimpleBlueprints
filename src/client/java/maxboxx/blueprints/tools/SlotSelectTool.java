package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.screens.ImportExportScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotSelectTool extends BlueprintTool {
	private final Identifier[] icons;

	private final List<BlueprintTool> slotTools = new ArrayList<>();

	public SlotSelectTool() {
		super((Identifier)null);

		icons = new Identifier[BlueprintManager.SLOT_COUNT];

		for (int i = 0; i < BlueprintManager.SLOT_COUNT; i++) {
			icons[i] = Identifier.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/slot" + (i + 1));
			slotTools.add(new SlotTool(this, i, icons[i]));
		}
	}

	@Override
	public Identifier getIcon() {
		return icons[BlueprintManager.getBlueprintSlot()];
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT   -> Optional.of(SimpleBlueprints.text("slots.change"));
			case RIGHT  -> Optional.of(SimpleBlueprints.text("slots.next_used"));
			case MIDDLE -> Optional.of(SimpleBlueprints.text("slots.first_empty"));
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				BlueprintTools.setSubTools(slotTools);
				BlueprintManager.setToolSlot(BlueprintManager.getBlueprintSlot());
			}

			case RIGHT -> {
				int slot = BlueprintManager.getBlueprintSlot();

				for (int i = 0; i < BlueprintManager.SLOT_COUNT; i++) {
					slot++;

					if (slot >= BlueprintManager.SLOT_COUNT) {
						slot = 0;
					}

					if (!BlueprintManager.isSlotEmpty(slot)) {
						BlueprintManager.setBlueprintSlot(slot);
						return;
					}
				}
			}

			case MIDDLE -> {
				for (int i = 0; i < BlueprintManager.SLOT_COUNT; i++) {
					if (BlueprintManager.isSlotEmpty(i)) {
						BlueprintManager.setBlueprintSlot(i);
						return;
					}
				}
			}
		}
	}

	private static class SlotTool extends BlueprintTool {
		public final int SLOT;
		public final SlotSelectTool PARENT_TOOL;

		public SlotTool(SlotSelectTool parent, int slot, Identifier icon) {
			super(icon);
			SLOT = slot;
			PARENT_TOOL = parent;
		}

		@Override
		public int getColor() {
			if (BlueprintManager.getBlueprintSlot() == SLOT) {
				return BLUE;
			}
			else if (BlueprintManager.hasDataInSlot(SLOT)) {
				return WHITE;
			}
			else if (BlueprintManager.hasSelectionForSlot(SLOT)) {
				return GRAY;
			}
			else {
				return GRAYED_OUT;
			}
		}

		@Override
		public Optional<Component> getAction(ToolAction action) {
			return switch (action) {
				case LEFT   -> Optional.of(SimpleBlueprints.text("slots.select"));
				case MIDDLE -> Optional.of(SimpleBlueprints.text("slots.import"));
				default -> Optional.empty();
			};
		}

		@Override
		public void performAction(LocalPlayer player, ToolAction action) {
			switch (action) {
				case LEFT -> {
					BlueprintTools.clearSubTools();
					BlueprintManager.setToolSlot(BlueprintTools.indexOfSafe(PARENT_TOOL));
					BlueprintManager.setBlueprintSlot(SLOT);
				}

				case MIDDLE -> {
					BlueprintManager.setBlueprintSlot(SLOT);
					Minecraft.getInstance().setScreen(new ImportExportScreen());
				}
			}
		}
	}
}
