package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class StartEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if(!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		info.getGame().start();
	}
}
