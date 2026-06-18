package maxboxx.blueprints.graphics.ui.screens;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.data.BlueprintBlockData;
import maxboxx.blueprints.graphics.ui.ItemRenderer;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemListScreen extends Screen {
	private List<BlueprintBlockData.ItemData> items;

	private Button closeButton;

	public ItemListScreen() {
		super(Component.empty());
	}

	@Override
	protected void init() {
		if (BlueprintManager.hasData()) {
			items = BlueprintManager.getData().getItems();
		}
		else {
			items = new ArrayList<>();
		}

		closeButton = Button.builder(Txt.key("item_list.close"),b -> {
			Minecraft.getInstance().gui.setScreen(null);
		}).width(120).build();

		addRenderableWidget(closeButton);
	}

	@Override
	public void extractBackground(@NotNull GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		closeButton.setPosition(context.guiWidth() / 2 - closeButton.getWidth() / 2, context.guiHeight() - 25);

		super.extractBackground(context, mouseX, mouseY, delta);

		ItemRenderer.renderGrid(context, items, BlueprintManager.getBlockListMode(), true);

		int index = ItemRenderer.getItemIndexAtPosition(width, height, items, mouseX, mouseY, true);

		if (index >= 0) {
			context.setTooltipForNextFrame(items.get(index).stack().getHoverName(), mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(@NotNull MouseButtonEvent event, boolean bl) {
		int index = ItemRenderer.getItemIndexAtPosition(width, height, items, (int)event.x(), (int)event.y(), true);

		if (index >= 0) {
			items.set(index, new BlueprintBlockData.ItemData(items.get(index).stack(), !items.get(index).visible()));
			return true;
		}

		return super.mouseClicked(event, bl);
	}
}
