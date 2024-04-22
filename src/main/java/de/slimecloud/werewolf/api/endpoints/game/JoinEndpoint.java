package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class JoinEndpoint implements Handler {
	public final static Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,16}$");

	@Getter
	public static class Request {
		private String name;
	}

	@AllArgsConstructor
	public static class Response {
		public final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) {
		Request request = ctx.bodyValidator(Request.class)
				.check(r -> NAME_PATTERN.asMatchPredicate().test(r.getName()), "Invalid 'name'")
				.get();

		Game game = ctx.appData(Server.MAIN_KEY).getGames().getIfPresent(ctx.queryParam("id"));

		if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		Player player = game.join(request.getName());

		ctx.json(new Response(ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(
				player.getId(),
				game.getId())
		));
	}
}
