package maxboxx.blueprints.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import maxboxx.blueprints.BlueprintManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HandRendererMixins {
	@Inject(at = @At("HEAD"), method = "renderHandsWithItems", cancellable = true)
	private void renderHandsWithItems(float f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LocalPlayer localPlayer, int i, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			info.cancel();
		}
	}
}
