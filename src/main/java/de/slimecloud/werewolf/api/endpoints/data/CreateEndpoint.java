package de.slimecloud.werewolf.api.endpoints.data;

import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.api.endpoints.game.JoinEndpoint;
import de.slimecloud.werewolf.data.Game;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class CreateEndpoint implements Handler {
	@Getter
	public static class Request {
		public String masterName;
	}

	@AllArgsConstructor
	public static class Response {
		public String game;
		public final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Request request = ctx.bodyValidator(CreateEndpoint.Request.class)
				.check(r -> JoinEndpoint.NAME_PATTERN.asMatchPredicate().test(r.getMasterName()), "Invalid 'masterName'")
				.get();

		Game game = ctx.appData(Server.MAIN_KEY).create(request.getMasterName());

		ctx.json(new Response(game.getId(), ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(
				game.getMaster(),
				game.getId())
		));
		game.sendUpdate();
	}
}
