package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class JoinEndpoint implements Handler {
	@Getter
	public class Request {
		private String name;
	}

	@AllArgsConstructor
	public class Response {
		public final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) {
		Request request = ctx.bodyValidator(Request.class)
				.check(r -> !r.getName().isBlank() && r.getName().length() > 4, "Invalid 'name'")
				.get();

		String id = ctx.pathParam("id");
		Player player = new Player(request.getName(), false);
		//TODO add player to game

		ctx.json(new Response(ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(player.getId().toString())));
	}
}
