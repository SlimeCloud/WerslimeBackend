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
		List<Integer> playerPool = new ArrayList<>();
		for (int i = 0; i < players.size(); i++) playerPool.add(i);

		Set<Integer> werewolfIndexes = new HashSet<>();
		for (int i = 0; i < settings.getWerewolfAmount(); i++) werewolfIndexes.add(removeRandom(playerPool));

		Map<Integer, Role> roleMap = new HashMap<>();
		List<Role> roles = settings.getRoles().stream().sorted(Comparator.comparingInt(Role::getPriority)).toList();

		for (Role role : roles) roleMap.put(removeRandom(playerPool), role);

		AtomicInteger i = new AtomicInteger();
		players.values().forEach(player -> {
			Role role;
			if (werewolfIndexes.contains(i.getAndIncrement())) role = Role.WEREWOLF;
			else role = roleMap.getOrDefault(i.get(), Role.VILLAGER);

			player.pushEvent(EventType.START, new StartEvent(this, role));
		});

		started = true;
	}

	private <T> T removeRandom(List<T> list) {
		if (list.isEmpty()) return null;
		return list.remove(Main.random.nextInt(list.size()-1));
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
