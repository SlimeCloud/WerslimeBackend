package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.NotImplementedResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class OEmbedEndpoint implements Handler {
	@RequiredArgsConstructor
	public static class Response {
		public final String version = "1.0";
		public final String type = "link";

		public final String provider_name = "Werslime";

		public final String author_name;
		public final String title;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		String format = ctx.queryParam("format");
		String url = ctx.queryParam("url");

		if (url == null || format == null) throw new BadRequestResponse();
		if (!format.equals("json")) throw new NotImplementedResponse();

		url = url.replace(ctx.appData(Server.MAIN_KEY).getConfig().getUrl(), "");

		if (url.matches("/game/[a-zA-Z0-9]+")) {
			String id = url.substring("/game/".length());
			Game game = ctx.appData(Server.MAIN_KEY).getGames().getIfPresent(id);

			if (game == null) ctx.json(new Response("Überprüfe die angegebene ID und stelle sicher, dass die Runde noch aktiv ist!", "Die gesuchte Runde wurde nicht gefunden!"));
			else ctx.json(new Response("Spiel-Leiter: " + game.getPlayers().values().stream().filter(Player::isMaster).findFirst().map(Player::getName).orElse(null), "Komm und spiel mit!"));
		} else if (url.matches("/join\\?token=.*")) {
			String token = url.substring("/join?token=".length());
			AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(token, true);

			ctx.json(new Response("Beitreten als: " + info.getPlayer().getName(), "Tritt diesem Spiel bei!"));
		} else ctx.json(new Response("-- Slimecloud --", "Spiele Werslime"));
	}
}
