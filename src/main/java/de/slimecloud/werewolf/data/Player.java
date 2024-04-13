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
		if (client != null) client.sendEvent("UPDATE", GameState.create(game, this), UUID.randomUUID().toString());
	}
}
