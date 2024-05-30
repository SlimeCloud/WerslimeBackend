package de.slimecloud.werewolf.api.endpoints.self;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.discord.DiscordGame;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class JoinEndpoint implements Handler {
	public final static Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,16}$");

	@Getter
	public static class Request {
		private String name;
		private String code;
	}

	@AllArgsConstructor
	public static class Response {
		public final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) {
		Request request = ctx.bodyValidator(Request.class).get();

		if ((request.getName() != null) == (request.getCode() != null)) throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST);

		Game game = ctx.appData(Server.MAIN_KEY).getGames().getIfPresent(ctx.pathParam("game_id"));
		if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		if (request.getName() != null) {
			if (!NAME_PATTERN.asMatchPredicate().test(request.getName())) throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST);
			Player player = game.join(request.getName());

			ctx.json(new Response(ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(player.getId(), game.getId())));
		} else {
			if (!(game instanceof DiscordGame dg)) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

			ctx.async(() -> {
				try {
					TokensResponse token = ctx.appData(Server.MAIN_KEY).getOauth2().getTokens(request.getCode());
					User user = new DiscordAPI(token.getAccessToken()).fetchUser();

					if (game.getPlayer(user.getId()).isEmpty()) dg.join(user.getId(), user.getUsername());
					ctx.json(new Response(ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(user.getId(), game.getId())));
				} catch (Exception e) {
					ctx.status(500);
				}
			});
		}
	}
}
