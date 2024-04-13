package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.event.Event;
import de.slimecloud.werewolf.api.event.EventType;
import de.slimecloud.werewolf.api.event.StartEvent;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;

	private final UUID id = UUID.randomUUID();
	private final Map<String, Player> players = new HashMap<>();
	private final UUID master;

	private boolean started = false;

	@Setter
	private GameSettings settings = new GameSettings();

	public void start() {
		Set<Integer> werewolfIndexes = new HashSet<>();
		for (int i = 0; i < settings.getWerewolfAmount(); i++) werewolfIndexes.add(Main.random.nextInt(players.size() - 1));

		AtomicInteger i = new AtomicInteger();
		players.values().forEach(player -> {
			Role role;
			if (werewolfIndexes.contains(i.getAndIncrement())) role = Role.WEREWOLF;
			else role = settings.getRoles()[Main.random.nextInt(settings.getRoles().length - 1)];

			player.pushEvent(EventType.START, new StartEvent(this, role));
		});

		started = true;
	}

	public void pushEvent(@NotNull EventType type, @NotNull Event event, @NotNull Predicate<Player> filter) {
		players.values().forEach(player -> {
			if (!filter.test(player)) return;
			player.pushEvent(type, event);
		});
	}

	public void pushEvent(@NotNull EventType type, @NotNull Event event) {
		pushEvent(type, event, p -> true);
	}
}
