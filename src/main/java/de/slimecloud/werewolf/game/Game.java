package de.slimecloud.werewolf.game;

import de.mineking.javautils.ID;
import de.slimecloud.werewolf.data.EventType;
import de.slimecloud.werewolf.data.ProtocolEntry;
import de.slimecloud.werewolf.data.Sound;
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
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class Game {
	protected final Main main;

	protected final String id;
	protected final Map<String, Player> players = new HashMap<>();

	protected boolean started;
	protected final GameSettings settings = GameSettings.createDefault();

	@Setter
	protected String victim;
	protected Role current = Role.VILLAGER;

	protected final Map<String, Object> interactions = new HashMap<>();
	protected final Map<Role, Object> roleMetaData = new HashMap<>();
	protected final Set<Runnable> nightActions = new HashSet<>();

	protected final List<ProtocolEntry> protocol = new ArrayList<>();

	public boolean isPublic() {
		return settings.isPublic();
	}

	@NotNull
	public Stream<Player> getPlayers() {
		return players.values().stream().filter(p -> !p.isMaster() || !settings.storyMode() || !started);
	}

	@NotNull
	public Map<String, Player> getAllPlayers() {
		return players;
	}

	@NotNull
	public Optional<Player> getPlayer(@NotNull String id) {
		return Optional.ofNullable(players.get(id));
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
			if (getPlayers().noneMatch(Player::isMaster)) main.getGames().invalidate(id); //Will automatically call cleanup due to removalListener

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
		players.values().forEach(Player::reset);

		current = Role.VILLAGER;
		victim = null;

		interactions.clear();
		roleMetaData.clear();
		protocol.clear();
	}

	public void start() {
		if (started) return;
		reset();

		started = true;
		int players = (int) getPlayers().count();

		List<Role> roles = new ArrayList<>(players);
		for (int i = 0; i < settings.werewolfAmount(); i++) roles.add(Role.WEREWOLF);

		roles.addAll(settings.roles().stream()
				.limit(Math.max(players - settings.werewolfAmount(), 0))
				.toList()
		);

		for (int i = roles.size(); i < getPlayers().count(); i++) roles.add(Role.VILLAGER);

		getPlayers().forEach(player -> player.setRole(roles.remove(Main.random.nextInt(roles.size()))));
		Role.values.forEach(r -> r.initialize(this));

		setCurrent(getNextRole(-1));
		current.onTurnStart(this);

		pushProtocol(ProtocolEntry.ProtocolType.START);

		playSound(Sound.START, 0.5);
		sendUpdate();
	}

	public void next() {
		if (!started) return;

		Role temp = current;
		current.onTurnEnd(this);

		if (temp == current) setCurrent(getNextRole(Role.values.indexOf(current)));

		checkWin();
		sendUpdate();
	}

	public void setCurrent(@NotNull Role role) {
		this.current = role;
		current.onTurnStart(this);
		interactions.clear();
	}

	public void scheduleNightAction(@NotNull Runnable action) {
		if (current.hasFlag(RoleFlag.DAY)) action.run();
		else synchronized (nightActions) {
			nightActions.add(action);
		}
	}

	public void pushProtocol(@NotNull ProtocolEntry.ProtocolType type, @NotNull Object[] data) {
		protocol.add(new ProtocolEntry(ID.generate().asString(), type, data));
	}

	public void pushProtocol(@NotNull ProtocolEntry.ProtocolType type) {
		pushProtocol(type, new Object[0]);
	}

	@NotNull
	@SuppressWarnings("unchecked")
	public <T> T getRoleMetaData(@NotNull Role role) {
		return (T) roleMetaData.get(role);
	}

	private void checkWin() {
		if (true) return;

		for (Team candidate : Team.values()) {
			if (candidate.isWinning(this)) {
				sendWin(candidate);
				return;
			}
		}
	}

	public void sendWin(@NotNull Team team) {
		if (!started) return;
		started = false;

		List<Player> winners = getPlayers()
				.filter(p -> p.hasTeam(team))
				.toList();

		List<Player> losers = getPlayers()
				.filter(p -> !winners.contains(p))
				.toList();

		winners.forEach(p -> p.playSound(Sound.WIN));
		losers.forEach(p -> p.playSound(Sound.LOSE));

		pushProtocol(ProtocolEntry.ProtocolType.END, new String[]{ team.name() });
		sendEvent(EventType.END, new GameEnding(team, winners.stream().map(Player::getId).toList()));
	}

	@NotNull
	public Optional<Player> evaluateVote() {
		Map<String, Double> votes = new HashMap<>();

		interactions.forEach((p, t) -> {
			Player player = players.get(p);
			Player target = players.get(t);

			if (player == null || target == null) return;
			double weight = player.getVoteCount();

			votes.compute(target.getId(), (k, v) -> v == null ? weight : v + weight);
		});

		return votes.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.filter(e -> votes.values().stream().filter(v -> Objects.equals(v, e.getValue())).count() == 1) // Ignore voting result on tie
				.map(Map.Entry::getKey).map(players::get);
	}

	public void sendEvent(@NotNull EventType type, @NotNull Object object) {
		players.values().forEach(p -> p.sendEvent(type, object));
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
		} while (!Role.values()[i.get()].canUseRole(this));

		return Role.values()[i.get()];
	}

	private record GameEnding(Team winner, List<String> players) {
	}

	public void cleanup() {
		sendEvent(EventType.CLOSE, new Object());
	}
}
