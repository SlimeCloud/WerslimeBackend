package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.GameInfo;
import de.slimecloud.werewolf.data.PlayerInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GameInfoEndpoint implements Handler {

	@AllArgsConstructor
	public class Response {
		public GameInfo game;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		Game game = ctx.appData(Server.MAIN_KEY).getGames().get(ctx.pathParam("id"));

		List<PlayerInfo> PlayerInfos = new ArrayList<>(); //ToDo create this List from game.getPlayers()

		GameInfo gameInfo = new GameInfo(game.getId().toString(), PlayerInfos, game.getMaster().toString(), game.isStarted());

		ctx.json(new Response(gameInfo));

	}
}
