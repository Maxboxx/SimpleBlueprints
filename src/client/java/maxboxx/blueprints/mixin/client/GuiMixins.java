package maxboxx.blueprints.mixin.client;

import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.BlueprintTools;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixins {
	@Inject(at = @At("HEAD"), method = "renderSlot", cancellable = true)
	private void renderSlot(GuiGraphics guiGraphics, int i, int j, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int k, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			BlueprintTool tool = BlueprintTools.get(k - 1);

			if (tool.getIcon() != null) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, tool.getIcon(), i, j, 16, 16, tool.getColor());
			}

			if (BlueprintManager.currentTool() == tool) {
				tool.getTooltip().ifPresent(tooltip -> {
					Font font = Minecraft.getInstance().font;

					guiGraphics.fill(i + 6 - font.width(tooltip) / 2, j - 18, i + 10 + font.width(tooltip) / 2, j - 6, 0xbb000000);
					guiGraphics.drawCenteredString(font, tooltip, i + 8, j - 16, 0xffffffff);
				});
			}

			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "renderSelectedItemName", cancellable = true)
	private void renderSelectedItemName(GuiGraphics guiGraphics, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			info.cancel();
		}
	}
}