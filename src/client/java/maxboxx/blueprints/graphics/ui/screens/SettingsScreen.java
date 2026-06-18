package maxboxx.blueprints.graphics.ui.screens;

import maxboxx.blueprints.data.Settings;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends BaseScreen {
	private final int PADDING = 20;
	private final int GAP = 10;
	private final int LABEL_WIDTH = 120;
	private final int INPUT_WIDTH = 120;
	private final int BUTTON_WIDTH = 80;
	private final int ROW_SPACING = 30;
	private final int INPUT_HEIGHT = 20;
	private final int TEXT_Y_OFFSET = 6;

	private Button closeButton;

	private final List<AbstractWidget> widgets = new ArrayList<>();

	public SettingsScreen() {
		super(Component.empty());
	}

	@Override
	protected void init() {
		super.init();

		widgets.clear();

		for (Settings.Setting<?> setting : Settings.getSettings()) {
			if (setting instanceof Settings.IntSetting intSetting) {
				NumberInput input = new NumberInput(font, INPUT_WIDTH, INPUT_HEIGHT, Component.empty());

				input.setPosition(PADDING + LABEL_WIDTH + GAP, PADDING + ROW_SPACING * widgets.size());

				input.setIntValue(intSetting.getValue());
				input.setMinValue(intSetting.MIN);
				input.setMaxValue(intSetting.MAX);

				input.setMaxLength(50);
				input.setCanLoseFocus(true);

				input.onChange(intSetting::setValue);
				widgets.add(input);
				addRenderableWidget(input);
			}
			else if (setting instanceof Settings.BooleanSetting boolSetting) {
				Button button = Button.builder(boolSetting.stateText(), b -> {
					boolSetting.setValue(!boolSetting.getValue());
					b.setMessage(boolSetting.stateText());
				}).pos(
					PADDING + LABEL_WIDTH + GAP,
					PADDING + ROW_SPACING * widgets.size()
				).width(BUTTON_WIDTH).build();

				widgets.add(button);
				addRenderableWidget(button);
			}
		}

		closeButton = Button.builder(Txt.key("settings.close"), b -> {
			Settings.save();
			Minecraft.getInstance().gui.setScreen(null);
		}).width(INPUT_WIDTH).build();

		addRenderableWidget(closeButton);
	}

	@Override
	public void onClose() {
		super.onClose();
		Settings.save();
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		closeButton.setPosition(context.guiWidth() / 2 - closeButton.getWidth() / 2, context.guiHeight() - 30);

		super.extractBackground(context, mouseX, mouseY, delta);

		Font font = Minecraft.getInstance().font;

		int row = 0;

		for (Settings.Setting<?> setting : Settings.getSettings()) {
			context.text(font, setting.name(), PADDING, PADDING + TEXT_Y_OFFSET + ROW_SPACING * row, 0xffffffff);
			row++;
		}
	}
}
