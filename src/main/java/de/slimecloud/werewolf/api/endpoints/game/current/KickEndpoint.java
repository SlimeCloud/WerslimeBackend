package de.slimecloud.werewolf.api.endpoints.game.current;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.game.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class KickEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		Player player = info.getGame().leave(ctx.pathParam("user_id"), p -> p.sendEvent("KICK", new Object()));
		if (player == null) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
	}
}
