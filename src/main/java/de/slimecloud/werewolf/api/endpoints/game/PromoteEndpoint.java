package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class PromoteEndpoint implements Handler {
	@Getter
	public static class Request {
		private String id;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		Request request = ctx.bodyValidator(Request.class)
				.check(r -> r.id != null, "Invalid 'id'")
				.get();

		Player player = info.getGame().getPlayers().get(request.getId());
		if (player == null) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

		player.setMaster(true);
		info.getGame().sendUpdate();
	}
}