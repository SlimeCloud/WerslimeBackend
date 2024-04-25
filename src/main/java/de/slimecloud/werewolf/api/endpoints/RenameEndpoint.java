package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.api.endpoints.game.JoinEndpoint;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class RenameEndpoint implements Handler {
	@Getter
	private static class Request {
		private String name;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (info.getGame().isStarted()) throw new ErrorResponse(ErrorResponseType.INVALID_GAME_STATE);

		Request request = ctx.bodyValidator(Request.class)
				.check(r -> JoinEndpoint.NAME_PATTERN.asMatchPredicate().test(r.getName()), "Invalid 'name'")
				.get();

		info.getPlayer().setName(request.getName());
		info.getGame().sendUpdate();
	}
}
