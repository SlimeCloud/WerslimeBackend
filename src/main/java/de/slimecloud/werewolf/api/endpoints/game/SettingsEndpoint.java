package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.GameSettings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SettingsEndpoint implements Handler {

	@Getter
	public static class Request {
		GameSettings settings;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Game game = ctx.appData(Server.MAIN_KEY).getGames().get(ctx.pathParam("id"));

		if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		game.setSettings(ctx.bodyAsClass(Request.class).getSettings());

	}
}
