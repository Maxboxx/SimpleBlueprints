package maxboxx.blueprints.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import maxboxx.blueprints.BlueprintManager;
import maxboxx.blueprints.tools.BlueprintTool;
import maxboxx.blueprints.tools.BlueprintTools;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
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

			if (tool.ICON != null) {
				guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, tool.ICON, i, j, 16, 16);
				//guiGraphics.blit(tool.ICON, i, j, 16, 16, 1f, 1f, 1f, 1f);
			}

			info.cancel();
		}
	}
}