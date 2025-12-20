package maxboxx.blueprints.tools;

import maxboxx.blueprints.SimpleBlueprints;
import maxboxx.blueprints.graphics.world.BoxGraphic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public abstract class BlueprintTool {
	public final ResourceLocation ICON;

	protected BlueprintTool(String iconName) {
		if (iconName == null) {
			ICON = null;
		}
		else {
			ICON = ResourceLocation.fromNamespaceAndPath(SimpleBlueprints.MOD_ID, "hud/" + iconName);
		}
	}

	public BoxGraphic.Mode getGraphicMode() {
		return BoxGraphic.Mode.NONE;
	}

	public abstract Optional<Component> getAction(ToolAction action);
	public abstract void performAction(LocalPlayer player, ToolAction action);
}
