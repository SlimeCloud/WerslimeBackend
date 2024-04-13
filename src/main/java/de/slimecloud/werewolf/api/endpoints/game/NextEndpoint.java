package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class NextEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if(!info.getGame().getMaster().equals(info.getPlayer().getId())) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);
		if(!info.getGame().isStarted()) throw new ErrorResponse(ErrorResponseType.GAME_NOT_STARTED);

		info.getGame().next();
	}
}
