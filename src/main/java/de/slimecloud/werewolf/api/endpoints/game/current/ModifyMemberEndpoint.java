package de.slimecloud.werewolf.api.endpoints.game.current;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Player;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ModifyMemberEndpoint implements Handler {
	@Getter
	public static class Request {
		private Boolean master;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		Request request = ctx.bodyValidator(Request.class).get();

		Player player = info.getGame().getPlayers().get(ctx.pathParam("user_id"));
		if (player == null) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

		if(request.getMaster() != null) player.setMaster(request.getMaster());

		info.getGame().sendUpdate();
	}
}
