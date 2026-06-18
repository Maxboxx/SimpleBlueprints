package maxboxx.blueprints.tools;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.ui.screens.ItemListScreen;
import maxboxx.blueprints.utils.Txt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class ItemListTool extends BlueprintTool {
	public enum CountMode {
		TOTAL,
		STACKS,
		CHESTS
	}

	public ItemListTool() {
		super("item_list");
	}

	@Override
	public boolean isAvailable() {
		return BlueprintManager.hasData();
	}

	@Override
	public Optional<Component> getAction(ToolAction action) {
		return switch (action) {
			case LEFT -> switch (BlueprintManager.getBlockListMode()) {
				case TOTAL  -> Optional.of(Txt.key("item_list.show_stacks"));
				case STACKS -> Optional.of(Txt.key("item_list.show_chests"));
				case CHESTS -> Optional.of(Txt.key("item_list.show_total"));
			};

			case RIGHT -> Optional.of(Txt.key("item_list.edit_items"));

			default -> Optional.empty();
		};
	}

	@Override
	public void performAction(LocalPlayer player, ToolAction action) {
		switch (action) {
			case LEFT -> {
				BlueprintManager.setBlockListMode(switch (BlueprintManager.getBlockListMode()) {
					case TOTAL  -> CountMode.STACKS;
					case STACKS -> CountMode.CHESTS;

					default -> CountMode.TOTAL;
				});
			}

			case RIGHT -> {
				Minecraft.getInstance().setScreenAndShow(new ItemListScreen());
			}
		}
	}
}
