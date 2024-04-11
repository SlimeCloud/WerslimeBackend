package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.GameInfo;
import de.slimecloud.werewolf.data.Player;
import de.slimecloud.werewolf.data.PlayerInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class MeEndpoint implements Handler {
	@AllArgsConstructor
	public static class Response {
		public final PlayerInfo player;
		public final GameInfo game;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);

		Game game = ctx.appData(Server.MAIN_KEY).getGames().get(info.getGame());
		if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		Player player = game.getPlayers().get(info.getUser());
		if (player == null) throw new ErrorResponse(ErrorResponseType.UNKNOWN); //This shouldn't happen because we never remove a player from the list and only sign tokens with a valid player

		ctx.json(new Response(PlayerInfo.create(player), GameInfo.crete(game)));
	}
}
