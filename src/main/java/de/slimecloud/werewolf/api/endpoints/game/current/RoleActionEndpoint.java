package de.slimecloud.werewolf.api.endpoints.game.current;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.game.RoleFlag;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class RoleActionEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);

		if (!info.getGame().getCurrent().hasRole(info.getPlayer())) throw new ErrorResponse(ErrorResponseType.INVALID_GAME_STATE);
		if (!info.getPlayer().isAlive()) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

		if (!info.getGame().getCurrent().hasFlag(RoleFlag.VOTE) && info.getGame().getInteractions().containsKey(info.getPlayer().getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

		info.getGame().getCurrent().handle(info.getPlayer(), ctx);
		if (info.getPlayer().getRole() == info.getGame().getCurrent()) info.getGame().getInteractions().putIfAbsent(info.getPlayer().getId(), new Object());

		info.getGame().sendUpdate();
	}
}
