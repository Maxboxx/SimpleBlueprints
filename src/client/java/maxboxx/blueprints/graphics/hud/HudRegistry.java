package maxboxx.blueprints.graphics.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class HudRegistry {
	public static void register(HudGraphic graphic) {
		HudElementRegistry.attachElementAfter(graphic.target(), graphic.id(), (context, ticks) -> {
			if (graphic.isHidden()) {
				return;
			}

			graphic.render(context);
		});
	}
}
