package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.event.Event;
import io.javalin.http.sse.SseClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
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
	private Boolean mayor = null;
	private Boolean alive = null;

	public void pushEvent(@NotNull Event event) {
		if(client != null) client.sendEvent(null, null, null);
	}
}
