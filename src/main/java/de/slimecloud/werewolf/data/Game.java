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
	private Role current = Role.VILLAGER;

	private final Map<String, Object> interactions = new HashMap<>();
	private final Map<Role, Object> roleMetaData = new HashMap<>();

	@NotNull
	public Player join(@NotNull String name) {
		Player player = new Player(name);

		if(started) {
			player.setRole(Role.VILLAGER);
			player.setAlive(this, false, null);
		}

		players.put(player.getId(), player);
		sendUpdate();

		return player;
	}

	@Nullable
	public Player leave(@NotNull String player, @Nullable Consumer<Player> event) {
		Player removed = players.remove(player);

		if (removed != null) {
			if (players.values().stream().noneMatch(Player::isMaster)) {
				main.getGames().invalidate(id);
				sendEvent("CLOSE", new Object());
			}

			if (event != null) event.accept(removed);

			removed.getClients().removeIf(ctx -> {
				ctx.closeSession();
				return true;
			});

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
			player.setLover(false);
		});

		current = Role.VILLAGER;
		victim = null;

		interactions.clear();
		roleMetaData.clear();
		Role.values.forEach(r -> r.initialize(this));
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

		if (current.isVote()) evaluateVote().ifPresent(player -> {
			switch (current) {
				case VILLAGER -> Optional.ofNullable(players.get(player)).ifPresent(p -> p.kill(this, KillReason.VILLAGE_VOTE));
				case WEREWOLF -> victim = player;
			}
		});

		current = getNextRole(Role.values.indexOf(current));

		if (current == Role.VILLAGER) {
			Optional.ofNullable(victim).map(players::get).ifPresent(p -> p.kill(this, KillReason.WEREWOLF_ATTACK));
			victim = null;
		}

		interactions.clear();

		checkMayor();

		checkWin();
		sendUpdate();
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <T> T getRoleMetaData(@NotNull Role role) {
		return (T) roleMetaData.get(role);
	}

	public int getPlayerCount() {
		return (int) players.values().stream()
				.filter(Player::isAlive)
				.count();
	}

	private void checkWin() {
		long wolves = players.values().stream().filter(p -> p.getTeam() == Team.WEREWOLF).filter(Player::isAlive).count();

		if (players.values().stream().filter(Player::isLover).filter(Player::isAlive).count() >= getPlayerCount() / 2.0 && players.values().stream().filter(p -> p.getTeam() == Team.WEREWOLF).filter(p -> !p.isLover()).noneMatch(Player::isAlive)) sendWin(Winner.LOVER);
		else if (wolves == 0) sendWin(Winner.VILLAGER);
		else if (wolves >= getPlayerCount() / 2.0) sendWin(Winner.WEREWOLF);
	}

	public void sendWin(@NotNull Winner winner) {
		sendEvent("END", new GameEnding(winner));
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
		if (players.values().stream().filter(Player::isAlive).noneMatch(Player::isMayor)) {
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

	private record GameEnding(Winner winner) {
	}
}
