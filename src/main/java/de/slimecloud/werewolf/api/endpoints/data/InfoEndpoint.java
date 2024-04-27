package de.slimecloud.werewolf.api.endpoints.data;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.GameInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class InfoEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		String id = ctx.pathParam("id");
		Game game = ctx.appData(Server.MAIN_KEY).getGames().getIfPresent(id);

		if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);
		ctx.json(GameInfo.create(game, null));
	}
}
