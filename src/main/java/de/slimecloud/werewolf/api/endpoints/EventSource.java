package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
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
			AuthorizationInfo info = main.getAuthenticator().checkAuthorization(ctx.queryParam("token"), true);

			if (info.getPlayer().getClient() != null) info.getPlayer().getClient().closeSession();

			ctx.enableAutomaticPings();
			info.getPlayer().setClient(ctx);
			info.getPlayer().sendUpdate(info.getGame());

			info.getGame().sendUpdate();
		});
		config.onClose(ctx -> {
			AuthorizationInfo info = main.getAuthenticator().checkAuthorization(ctx.queryParam("token"), true);

			info.getPlayer().setClient(null);
			info.getGame().sendUpdate();
		});
	}
}
