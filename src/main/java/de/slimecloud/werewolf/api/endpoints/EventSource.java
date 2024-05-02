package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.main.Main;
import io.javalin.websocket.WsConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@Getter
@RequiredArgsConstructor
public class EventSource implements Consumer<WsConfig> {
	private final Main main;

	@Override
	public void accept(@NotNull WsConfig config) {
		config.onConnect(ctx -> {
			try {
				AuthorizationInfo info = main.getAuthenticator().checkAuthorization(ctx.queryParam("token"), true);

				ctx.enableAutomaticPings();
				info.getPlayer().getClients().add(ctx);
				info.getPlayer().sendUpdate();

				info.getGame().sendUpdate();
			} catch (ErrorResponse e) {
				ctx.closeSession(4000 + e.status, e.type.name());
			} catch (Exception e) {
				logger.error("Failed to handle websocket connection", e);
				ctx.closeSession();
			}
		});
		config.onClose(ctx -> {
			AuthorizationInfo info = main.getAuthenticator().checkAuthorization(ctx.queryParam("token"), true);

			info.getPlayer().getClients().remove(ctx);
			info.getGame().sendUpdate();
		});
	}
}
