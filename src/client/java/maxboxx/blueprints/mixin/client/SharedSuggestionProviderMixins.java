package maxboxx.blueprints.mixin.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import maxboxx.blueprints.BlueprintManager;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Mixin(SharedSuggestionProvider.class)
public interface SharedSuggestionProviderMixins {
	@Invoker("suggest")
	static CompletableFuture<Suggestions> invokeSuggest(Iterable<String> iterable, SuggestionsBuilder suggestionsBuilder) {
		throw new AssertionError();
	}

	@Inject(at = @At("HEAD"), method = "suggestCoordinates", cancellable = true)
	private static void suggestCoordinates(String string, Collection<SharedSuggestionProvider.TextCoordinates> collection, SuggestionsBuilder suggestionsBuilder, Predicate<String> predicate, CallbackInfoReturnable<CompletableFuture<Suggestions>> callBackInfo) {
		if (BlueprintManager.isActive() && BlueprintManager.hasSelection()) {
			ArrayList<String> list = new ArrayList<>();

			BlockPos min = BlueprintManager.getSelectionMin();
			BlockPos max = BlueprintManager.getSelectionMax();

			list.add(min.getX() + " " + min.getY() + " " + min.getZ());
			list.add(max.getX() + " " + max.getY() + " " + max.getZ());

			callBackInfo.setReturnValue(invokeSuggest(list, suggestionsBuilder));
		}
	}
}
