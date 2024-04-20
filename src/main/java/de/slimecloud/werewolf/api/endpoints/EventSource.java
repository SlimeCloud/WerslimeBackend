package de.slimecloud.werewolf.api.endpoints;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.Server;
import io.javalin.http.sse.SseClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

@Slf4j
@Getter
@RequiredArgsConstructor
public class EventSource implements Consumer<SseClient> {
	@Override
	public void accept(@NotNull SseClient client) {
		AuthorizationInfo info = client.ctx().appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(client.ctx().queryParam("token"), true);

		if(info.getPlayer().getClient() != null) info.getPlayer().getClient().close();

		client.keepAlive();
		client.onClose(() -> {
			info.getPlayer().setClient(null);
			info.getGame().sendUpdate();
		});

		info.getPlayer().setClient(client);
		info.getPlayer().sendUpdate(info.getGame());

		info.getGame().sendUpdate();
	}
}
