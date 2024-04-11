package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.main.Main;
import io.javalin.websocket.WsContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;
	private final Map<WsContext, Player> connections = new HashMap<>();

	private final UUID id = UUID.randomUUID();
	private final Map<String, Player> players = new HashMap<>();
	private final UUID master;
	private final UUID mayor;
	private final boolean started = false;

	@Setter
	private GameSettings settings = new GameSettings();

	public void pushEvent(@NotNull Object event, @NotNull Predicate<Player> filter) {
		connections.forEach((context, player) -> {
			if(!filter.test(player)) return;
			context.send(event);
		});
	}

	public void pushEvent(@NotNull Object event) {
		pushEvent(event, p -> true);
	}
}
