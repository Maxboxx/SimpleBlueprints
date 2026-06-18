package maxboxx.blueprints.mixin.client;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.BlueprintTools;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixins {
	@Inject(at = @At("HEAD"), method = "extractSlot", cancellable = true)
	private void extractSlot(GuiGraphicsExtractor guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			BlueprintTool tool = BlueprintTools.get(seed - 1);

			if (tool.getIcon() != null) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, tool.getIcon(), x, y, 16, 16, tool.getColor());
			}

			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "extractSelectedItemName", cancellable = true)
	private void extractSelectedItemName(final GuiGraphicsExtractor guiGraphics, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			info.cancel();
		}
	}
}