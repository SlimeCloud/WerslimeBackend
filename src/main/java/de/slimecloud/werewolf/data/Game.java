package de.slimecloud.werewolf.data;

import de.mineking.javautils.ID;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public class Game {
	private final Main main;

	private final String id = ID.generate().asString();
	private final String master;

	private final Map<String, Player> players = new HashMap<>();
	private boolean started;

	@Setter
	private GameSettings settings = GameSettings.DEFAULT;

	@Setter
	private String victim;
	@Setter
	private Role current;

	private final Map<String, Object> interactions = new HashMap<>();
	private final Map<Role, Object> roleMetaData = new HashMap<>();

	@NotNull
	public Player join(@NotNull String name) {
		Player player = new Player(name, false);
		players.put(player.getId(), player);
		sendUpdate();

		return player;
	}

	@Nullable
	public Player leave(@NotNull String player, @Nullable Consumer<Player> event) {
		Player removed = players.remove(player);

		if(removed != null) {
			if (players.values().stream().noneMatch(Player::isMaster)) {
				main.getGames().remove(id);
				sendEvent("CLOSE", new Object());
			}

			if(event != null) event.accept(removed);

			if(removed.getClient() != null) removed.getClient().close();
			removed.setClient(null);

			sendUpdate();
		}

		return removed;
	}

	public void reset() {
		started = false;
		players.values().forEach(player -> {
			player.setRole(null);
			player.revive(this);
			player.setMayor(false);
		});

		current = null;
		victim = null;

		interactions.clear();
		roleMetaData.clear();
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

		current = getNextRole(-1);
		started = true;

		checkMayor();
		sendUpdate();
	}

	public void next() {
		if (!started) return;

		evaluateVote().ifPresent(player -> {
			switch (current) {
				case VILLAGER -> Optional.ofNullable(players.get(player)).ifPresent(p -> p.kill(this));
				case WEREWOLF -> victim = player;
			}
		});

		current = getNextRole(Role.values.indexOf(current));

		if (current == Role.VILLAGER) {
			Optional.ofNullable(victim).map(players::get).ifPresent(p -> p.kill(this));
			victim = null;
		}

		interactions.clear();

		checkMayor();

		checkWin();
		sendUpdate();
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <T> T getRoleMetaData(@NotNull Role role, @NotNull Supplier<T> defaultValue) {
		roleMetaData.putIfAbsent(role, defaultValue.get());
		return (T) roleMetaData.get(role);
	}

	public int getPlayerCount() {
		return (int) players.values().stream()
				.filter(Player::isAlive)
				.count();
	}

	private void checkWin() {
		long wolves = players.values().stream().filter(p -> p.getRole() == Role.WEREWOLF).count();

		if (wolves == 0) sendEvent("END", new GameEnding(Role.VILLAGER));
		else if (wolves >= getPlayerCount() / 2) sendEvent("END", new GameEnding(Role.WEREWOLF));

		reset();
	}

	@NotNull
	private Optional<String> evaluateVote() {
		Map<String, Double> votes = new HashMap<>();

		interactions.values().stream().map(t -> (String) t).forEach(p -> {
			double weight = Optional.ofNullable(players.get(p)).filter(Player::isMayor).map(m -> 1.5).orElse(1.0);
			votes.compute(p, (k, v) -> v == null ? weight : v + weight);
		});

		return votes.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.findAny().map(Map.Entry::getKey);
	}

	private void checkMayor() {
		if (players.values().stream().noneMatch(Player::isMayor)) {
			new ArrayList<>(players.values()).get(Main.random.nextInt(players.size())).setMayor(true);
		}
	}

	public void sendEvent(@NotNull String name, @NotNull Object object) {
		players.values().forEach(p -> p.sendEvent(name, object));
	}

	public void sendUpdate() {
		players.values().forEach(p -> p.sendUpdate(this));
	}

	@NotNull
	private Role getNextRole(int current) {
		AtomicInteger i = new AtomicInteger(current);
		int j = 0;

		do {
			i.updateAndGet(t -> (t + 1) % Role.values().length);
			if (j++ > Role.values().length * 2) {
				reset();
				break;
			}
		} while (!Role.values()[i.get()].canUseRole(this) || (Role.values()[i.get()] != Role.VILLAGER && players.values().stream().noneMatch(p -> p.isAlive() && p.getRole() == Role.values()[i.get()])));

		return Role.values()[i.get()];
	}

	private record GameEnding(Role winner) {
	}
}
