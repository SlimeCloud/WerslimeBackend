package de.slimecloud.werewolf.data;

import de.mineking.javautils.ID;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.eclipse.jetty.websocket.core.CloseStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class Game {
	protected final Main main;

	protected final String id;

	protected final Map<String, Player> players = new HashMap<>();

	protected boolean started;

	protected final GameSettings settings = GameSettings.DEFAULT;

	@Setter
	protected String victim;
	protected Role current = Role.VILLAGER;

	protected final Map<String, Object> interactions = new HashMap<>();
	protected final Map<Role, Object> roleMetaData = new HashMap<>();
	protected final Set<Runnable> nightActions = new HashSet<>();

	public boolean isPublic() {
		return settings.isPublic();
	}

	@NotNull
	public Player join(@NotNull String name) {
		Player player = new Player(this, ID.generate().asString(), name);

		if (started) {
			player.setRole(Role.VILLAGER);
			player.setAlive(false, null);
		}

		players.put(player.getId(), player);
		sendUpdate();

		return player;
	}

	@Nullable
	public Player leave(@NotNull String player, @Nullable Consumer<Player> event) {
		Player removed = players.remove(player);

		if (removed != null) {
			if (players.values().stream().noneMatch(Player::isMaster)) main.getGames().invalidate(id); //Will automatically call cleanup due to removalListener

			if (event != null) event.accept(removed);

			removed.getClients().removeIf(ctx -> {
				ctx.closeSession(CloseStatus.NORMAL, "leave");
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
			player.revive();
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
		for (int i = 0; i < settings.werewolfAmount(); i++) roles.add(Role.WEREWOLF);

		roles.addAll(settings.roles().stream()
				.sorted(Comparator.comparing(Role::getPriority, Comparator.reverseOrder()))
				.limit(Math.max(players.size() - settings.werewolfAmount(), 0))
				.toList()
		);

		for (int i = roles.size(); i < players.size(); i++) roles.add(Role.VILLAGER);

		players.values().forEach(player -> player.setRole(roles.remove(Main.random.nextInt(roles.size()))));

		setCurrent(getNextRole(-1));
		current.onTurnStart(this);

		started = true;

		playSound(Sound.START);
		sendUpdate();
	}

	public void next() {
		if (!started) return;

		Role temp = current;
		current.onTurnEnd(this);

		if (temp == current) {
			if (current == Role.HUNTER) current = getRoleMetaData(Role.HUNTER);
			setCurrent(getNextRole(Role.values.indexOf(current)));
		}

		if (current.isDay()) synchronized (nightActions) {
			nightActions.forEach(Runnable::run);
			nightActions.clear();

			Optional.ofNullable(victim).map(players::get).ifPresent(p -> p.kill(KillReason.WEREWOLF_ATTACK));
			victim = null;
		}

		checkWin();

		sendUpdate();
	}

	public void setCurrent(@NotNull Role role) {
		this.current = role;
		current.onTurnStart(this);
		interactions.clear();
	}

	public void scheduleNightAction(@NotNull Runnable action) {
		if (current.isDay()) action.run();
		else synchronized (nightActions) {
			nightActions.add(action);
		}
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <T> T getRoleMetaData(@NotNull Role role) {
		return (T) roleMetaData.get(role);
	}

	private void checkWin() {
		for (Winner candidate : Winner.values()) {
			if (candidate.isWinning(this)) {
				sendWin(candidate);
				return;
			}
		}
	}

	public void sendWin(@NotNull Winner winner) {
		players.values().stream()
				.filter(winner::isMember)
				.forEach(p -> p.playSound(Sound.WIN));
		sendEvent("END", new GameEnding(winner));
	}

	@NotNull
	public Optional<Player> evaluateVote() {
		Map<String, Double> votes = new HashMap<>();

		interactions.forEach((p, t) -> {
			Player player = players.get(p);
			Player target = players.get(t);

			if (player == null || target == null) return;
			double weight = player.voteCount(current);

			votes.compute(target.getId(), (k, v) -> v == null ? weight : v + weight);
		});

		return votes.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.filter(e -> votes.values().stream().filter(v -> Objects.equals(v, e.getValue())).count() == 1) // Ignore voting result on tie
				.map(Map.Entry::getKey).map(players::get);
	}

	public void sendEvent(@NotNull String name, @NotNull Object object) {
		players.values().forEach(p -> p.sendEvent(name, object));
	}

	public void sendUpdate() {
		players.values().forEach(Player::sendUpdate);
	}

	public void playSound(@NotNull Sound sound) {
		playSound(sound, 1);
	}

	public void playSound(@NotNull Sound sound, double volume) {
		players.values().forEach(p -> p.playSound(sound, volume));
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
		} while (!Role.values()[i.get()].canUseRole(this) || players.values().stream().filter(Player::isAlive).noneMatch(Role.values()[i.get()]::hasRole));

		return Role.values()[i.get()];
	}

	private record GameEnding(Winner winner) {
	}

	public void cleanup() {
		sendEvent("CLOSE", new Object());
	}
}
