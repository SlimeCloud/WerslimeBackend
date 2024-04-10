package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class CreateEndpoint implements Handler {

	@Getter
	public class Request {
		public String masterName;
	}

	@AllArgsConstructor
	public class Response {
		public String game;
		public final String token;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		CreateEndpoint.Request request = ctx.bodyValidator(CreateEndpoint.Request.class)
				.check(r -> !r.getMasterName().isBlank() && r.getMasterName().length() > 4, "Invalid 'name'")
				.get();

		Player master = new Player(request.getMasterName(), true);
		Game game = new Game(master.getId());

		ctx.appData(Server.MAIN_KEY).getGames().put(game.getId().toString(), game);

		ctx.json(new Response(game.getId().toString(), ctx.appData(Server.MAIN_KEY).getAuthenticator().generateToken(master.getId().toString())));
	}
}
