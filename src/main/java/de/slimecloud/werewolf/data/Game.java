package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.data.request.WitchRequest;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;

	private final UUID id = UUID.randomUUID();
	private final Map<String, Player> players = new HashMap<>();
	private final UUID master;

	@Setter
	private GameSettings settings = GameSettings.DEFAULT;

	private boolean started;

	@Setter
	private String victim;
	private Role current;
	private EnumSet<WitchRequest.WitchAction> witchActions;

	@NotNull
	public Player join(@NotNull String name) {
		Player player = new Player(false, name);
		players.put(player.getId().toString(), player);
		sendUpdate();

		return player;
	}

	public Player leave(@NotNull String player) {
		Player removed = players.remove(player);
		sendUpdate();
		return removed;
	}

	public void reset() {
		started = false;
		witchActions = EnumSet.allOf(WitchRequest.WitchAction.class);

		current = null;
		victim = null;

		players.values().forEach(player -> {
			player.setAlive(true);
			player.setMayor(false);
		});
	}

	public void start() {
		if (started) return;

		reset();

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

	public void next() {
		if (!started) return;

		current = Role.WEREWOLF;

		sendUpdate();

		//TODO lifecycle
	}

	public void sendUpdate() {
		players.values().forEach(p -> p.sendUpdate(this));
	}
}
