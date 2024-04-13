package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.GameSettings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class SettingsEndpoint implements Handler {
	@Getter
	public static class Request {
		public GameSettings settings;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		if(!info.getGame().getId().toString().equals(ctx.pathParam("id"))) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		info.getGame().setSettings(ctx.bodyAsClass(Request.class).getSettings());
	}
}