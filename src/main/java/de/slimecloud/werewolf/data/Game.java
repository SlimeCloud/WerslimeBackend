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
	@Setter
	private Role current;

	private EnumSet<WitchRequest.WitchAction> witchActions;
	private final Set<String> seerVisible = new HashSet<>();
	private final Map<String, String> votes = new HashMap<>();
	private final Set<String> interacted = new HashSet<>();

	@NotNull
	public Player join(@NotNull String name) {
		Player player = new Player(false, name);
		players.put(player.getId().toString(), player);
		sendUpdate();

		return player;
	}

	@NotNull
	public Player leave(@NotNull String player) {
		Player removed = players.remove(player);
		sendUpdate();
		return removed;
	}

	public void reset() {
		started = false;
		witchActions = EnumSet.allOf(WitchRequest.WitchAction.class);

		current = Role.WEREWOLF;
		victim = null;

		players.values().forEach(player -> {
			player.setRole(null);
			player.revive(this);
			player.setMayor(false);
		});

		seerVisible.clear();
		votes.clear();
		interacted.clear();
	}

	public void start() {
		if (started) return;

		reset();

		List<Role> roles = new ArrayList<>(players.size());
		for (int i = 0; i < settings.getWerewolfAmount(); i++) roles.add(Role.WEREWOLF);

		roles.addAll(settings.getRoles().stream()
				.sorted(Comparator.comparing(Role::getPriority, Comparator.reverseOrder()))
				.limit(Math.max(players.size() - settings.getWerewolfAmount(), 0))
				.toList()
		);

		for (int i = roles.size(); i < players.size(); i++) roles.add(Role.VILLAGER);

		players.values().forEach(player -> player.setRole(roles.remove(Main.random.nextInt(roles.size()))));

		started = true;

		checkMayor();
		sendUpdate();
	}

	public void next() {
		if (!started) return;

		getVoted().ifPresent(player -> {
			switch (current) {
				case VILLAGER -> getVoted().map(players::get).ifPresent(p -> p.kill(this));
				case WEREWOLF -> victim = player;
			}
		});

		int i = Role.values.indexOf(current);
		while (!Role.values()[i++ % Role.values().length].isAutomatic()) ;

		current = Role.values()[i];

		if (current == Role.VILLAGER) Optional.ofNullable(victim).map(players::get).ifPresent(p -> p.kill(this));

		votes.clear();
		interacted.clear();
		checkMayor();

		sendUpdate();
	}

	@NotNull
	private Optional<String> getVoted() {
		Map<String, Integer> votes = new HashMap<>();
		this.votes.values().forEach(t -> votes.compute(t, (k, v) -> v == null ? 1 : v + 1));
		return votes.entrySet().stream()
				.sorted(Map.Entry.comparingByValue())
				.findAny().map(Map.Entry::getKey);
	}

	private void checkMayor() {
		if (players.values().stream().noneMatch(Player::isMayor)) {
			new ArrayList<>(players.values()).get(Main.random.nextInt(players.size())).setMayor(true);
		}
	}

	public void sendUpdate() {
		players.values().forEach(p -> p.sendUpdate(this));
	}
}
