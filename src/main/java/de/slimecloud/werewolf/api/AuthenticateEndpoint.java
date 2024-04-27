package de.slimecloud.werewolf.api;

import de.slimecloud.werewolf.data.discord.DiscordGame;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.mokulu.discord.oauth.DiscordAPI;
import io.mokulu.discord.oauth.model.TokensResponse;
import io.mokulu.discord.oauth.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

public class AuthenticateEndpoint implements Handler {
	@Getter
	private static class Request {
		private String code;
		private String game;
	}

	@AllArgsConstructor
	private static class Response {
		private final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Request request = ctx.bodyValidator(Request.class)
				.check(r -> r.getCode() != null, "Invalid 'code'")
				.check(r -> r.getGame() != null, "Invalid 'game'")
				.get();

		if (!(ctx.appData(Server.MAIN_KEY).getGames().getIfPresent(request.getGame()) instanceof DiscordGame game)) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		ctx.async(() -> {
			try {
				TokensResponse token = ctx.appData(Server.MAIN_KEY).getOauth2().getTokens(request.getCode());
				User user = new DiscordAPI(token.getAccessToken()).fetchUser();

				if (!game.getPlayers().containsKey(user.getId())) game.join(user.getId(), user.getUsername());
				ctx.json(new Response(ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(user.getId(), game.getId())));
			} catch (Exception e) {
				ctx.status(500);
			}
		});
	}
}
