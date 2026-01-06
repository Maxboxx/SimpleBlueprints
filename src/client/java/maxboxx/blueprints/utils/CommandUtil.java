package maxboxx.blueprints.utils;

import net.minecraft.client.player.LocalPlayer;

public class CommandUtil {
	public static void sendCommand(LocalPlayer player, String commandString) {
		player.connection.sendCommand(commandString);
	}

	public static void sendCommand(LocalPlayer player, String commandName, Object... args) {
		StringBuilder builder = new StringBuilder(commandName);

		for (Object arg : args) {
			builder.append(" ");
			builder.append(arg);
		}

		sendCommand(player, builder.toString());
	}
}
