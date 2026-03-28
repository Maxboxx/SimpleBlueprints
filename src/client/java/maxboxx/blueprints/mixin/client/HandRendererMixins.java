package maxboxx.blueprints.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import maxboxx.blueprints.BlueprintManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HandRendererMixins {
	@Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
	private void renderArmWithItem(final AbstractClientPlayer player, final float frameInterp, final float xRot, final InteractionHand hand, final float attack, final ItemStack itemStack, final float inverseArmHeight, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final int lightCoords, CallbackInfo info) {
		if (BlueprintManager.isActive()) {
			info.cancel();
		}
	}
}
