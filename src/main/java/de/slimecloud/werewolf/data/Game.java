package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.event.Event;
import de.slimecloud.werewolf.api.event.EventType;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;

	private final UUID id = UUID.randomUUID();
	private final Map<String, Player> players = new HashMap<>();
	private final UUID master;

	private final boolean started = false;

	@Setter
	private GameSettings settings = new GameSettings();

	public void start() {
		//TODO set roles

	}

	public void pushEvent(@NotNull EventType type,  @NotNull Event event, @NotNull Predicate<Player> filter) {
		players.values().forEach(player -> {
			if(!filter.test(player)) return;
			player.pushEvent(type, event);
		});
	}

	public void pushEvent(@NotNull EventType type, @NotNull Event event) {
		pushEvent(type, event, p -> true);
	}
}
