package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;

	private final UUID id = UUID.randomUUID();
	private final Map<String, Player> players = new HashMap<>();
	private final UUID master;

	private boolean started = false;
	private Role current = null;

	@Setter
	private GameSettings settings = new GameSettings();

	public void start() {
		List<Role> roles = new ArrayList<>(players.size());
		for (int i = 0; i < settings.getWerewolfAmount(); i++) roles.add(Role.WEREWOLF);

		roles.addAll(settings.getRoles().stream()
				.sorted(Comparator.comparing(Role::getPriority, Comparator.reverseOrder()))
				.limit(players.size() - settings.getWerewolfAmount())
				.toList()
		);

		for (int i = roles.size(); i < players.size(); i++) roles.add(Role.VILLAGER);

		players.values().forEach(player -> player.setRole(roles.remove(Main.random.nextInt(roles.size()))));

		started = true;
		sendUpdate();
	}

	public void sendUpdate() {
		players.values().forEach(p -> p.sendUpdate(this));
	}
}
