package de.slimecloud.werewolf.data;

import io.javalin.http.sse.SseClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
public class Player {
	private final UUID id = UUID.randomUUID();
	private final boolean master;
	private final String name;

	private SseClient client = null;

	private Role role = null;
	private boolean mayor = false;
	private boolean alive = false;

	public void sendUpdate(@NotNull Game game) {
		sendEvent("UPDATE", GameState.create(game, this));
	}

	public void sendEvent(@NotNull String name, @NotNull Object data) {
		if (client != null) client.sendEvent(name, data, UUID.randomUUID().toString());
	}
}
