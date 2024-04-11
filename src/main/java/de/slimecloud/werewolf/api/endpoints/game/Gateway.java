package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import de.slimecloud.werewolf.main.Main;
import io.javalin.websocket.WsCloseStatus;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Gateway implements Consumer<WsConfig> {
	private final Main main;

	@Override
	public void accept(@NotNull WsConfig config) {
		config.onConnect(WsContext::enableAutomaticPings);
		config.onMessage(ctx -> {
			try {
				AuthorizationInfo info = main.getAuthenticator().checkAuthorization(ctx.message(), true);

				Game game = main.getGames().get(info.getGame());
				if (game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

				Player player = game.getPlayers().get(info.getUser());
				if (player == null) throw new ErrorResponse(ErrorResponseType.UNKNOWN);

				ctx.attribute("game", game);
				ctx.attribute("player", player);

				game.getConnections().put(ctx, player);
			} catch (Exception e) {
				ctx.closeSession(WsCloseStatus.PROTOCOL_ERROR, e.getMessage());
				logger.warn("Received invalid request", e);
			}
		});
		config.onClose(ctx -> {
			Game game = ctx.attribute("game");
			game.getConnections().remove(ctx);
		});
	}
}
