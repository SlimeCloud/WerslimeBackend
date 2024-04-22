package de.slimecloud.werewolf.api.endpoints.data;

import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.GameInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class ListEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		ctx.json(ctx.appData(Server.MAIN_KEY).getGames().asMap().values().stream()
				.filter(g -> g.getSettings().isPublic())
				.map(g -> GameInfo.create(g, null))
				.toList()
		);
	}
}
