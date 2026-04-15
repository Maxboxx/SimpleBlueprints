package maxboxx.blueprints.graphics.ui.screens;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.data.SlotProperties;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SlotOptionsScreen extends Screen {
	private static final int WIDGET_WIDTH = 120;

	private EditBox nameField;
	private Button persistenceButton;
	private Button closeButton;

	private final int slot;
	private final String slotName;
	private final SlotProperties.SlotData slotData;
	private boolean persistent;

	public SlotOptionsScreen(int slot) {
		super(Component.empty());
		this.slot = slot;
		this.slotData = BlueprintManager.getSlotProperties(slot);
		this.persistent = slotData.persistent();
		this.slotName = BlueprintManager.getSlotName(slot);
	}

	@Override
	protected void init() {
		nameField = new EditBox(font, 0, 0, Component.empty());
		nameField.setWidth(WIDGET_WIDTH);
		nameField.setHeight(20);
		nameField.setValue(slotName);

		persistenceButton = Button.builder(Txt.key(persistent ? "slot_options.persistent_on" : "slot_options.persistent_off"), b -> {
			persistent = !persistent;
			persistenceButton.setMessage(Txt.key(persistent ? "slot_options.persistent_on" : "slot_options.persistent_off"));
		}).width(WIDGET_WIDTH).tooltip(Tooltip.create(Txt.key("slot_options.persistent_desc"))).build();

		closeButton = Button.builder(Txt.key("slot_options.cleanup"), b -> {
			String newName = nameField.getValue();

			if (!newName.equals(slotName)) {
				BlueprintManager.setSlotName(slot, newName);
			}

			if ( persistent != slotData.persistent()) {
				BlueprintManager.setSlotProperties(slot, new SlotProperties.SlotData(
					persistent
				));
			}

			Minecraft.getInstance().setScreen(null);
		}).width(WIDGET_WIDTH).build();

		addRenderableWidget(nameField);
		addRenderableWidget(persistenceButton);
		addRenderableWidget(closeButton);
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		int originX = context.guiWidth() / 2;
		int originY = context.guiHeight() / 2;

		nameField.setPosition(originX - nameField.getWidth() / 2, originY);
		persistenceButton.setPosition(originX - closeButton.getWidth() / 2, originY + 30);
		closeButton.setPosition(originX - closeButton.getWidth() / 2, originY + 60);

		super.extractBackground(context, mouseX, mouseY, delta);

		context.text(font, Txt.key("slot_options.name"), originX - WIDGET_WIDTH / 2, nameField.getY() - 12, 0xffffffff);
	}
}
