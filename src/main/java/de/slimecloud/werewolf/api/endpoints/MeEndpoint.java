package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.GameInfo;
import de.slimecloud.werewolf.data.PlayerInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class MeEndpoint implements Handler {
	@AllArgsConstructor
	public static class Response {
		public final PlayerInfo player;
		public final GameInfo game;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		ctx.json(new Response(PlayerInfo.create(info.getPlayer()), GameInfo.create(info.getGame())));
	}
}
