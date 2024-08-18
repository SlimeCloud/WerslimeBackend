package de.slimecloud.werewolf.game;

import de.slimecloud.werewolf.data.*;
import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.channels.ClosedChannelException;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Setter
@Getter
@RequiredArgsConstructor
public class Player {
	protected final Game game;
	protected final String id;

	@NotNull
	protected String name;
	protected boolean master = false;

	protected Role role = Role.VILLAGER;
	protected final List<Team> teams = new ArrayList<>();
	protected final Set<Modifier> modifiers = new HashSet<>();

	@Setter(AccessLevel.NONE)
	protected boolean alive = false;

	protected Set<WsContext> clients = new HashSet<>();

	public void reset() {
		this.role = Role.VILLAGER;
		this.teams.clear();
		this.modifiers.clear();
		this.alive = !master || !game.getSettings().storyMode();
	}

	@Nullable
	public String getAvatar() {
		return null;
	}

	public boolean isSpectating() {
		return (!alive && game.getSettings().deadSpectators()) || !game.started || (master && game.getSettings().storyMode());
	}

	public boolean canSeeVictim() {
		return isSpectating() || (role.hasFlag(RoleFlag.VICTIM) && role == game.current);
	}

	public double getVoteCount() {
		double vote = role.getVoteMultiplier(this);

		for (IPlayerModifier m : getBehavior().toList()) vote *= m.getVoteMultiplier(this);

		return vote;
	}

	@NotNull
	public List<Team> getTeams() {
		return Stream.concat(teams.stream(), role.getTeams().stream().filter(t -> !teams.contains(t))).toList();
	}

	public boolean hasTeam(@NotNull Team team) {
		return teams.contains(team) || role.getTeams().contains(team);
	}

	public boolean hasModifier(@NotNull Modifier modifier) {
		return modifiers.contains(modifier);
	}

	public boolean hasFlag(@NotNull RoleFlag flag) {
		return role.getFlags().contains(flag);
	}

	public void addTeam(@NotNull Team team) {
		if (!teams.contains(team)) teams.add(team);
	}

	@NotNull
	public Collection<Modifier> getModifiers(@Nullable Player other) {
		if (other == null || other.isSpectating()) return modifiers;
		return modifiers.stream().filter(m -> m.isVisible(other)).toList();
	}

	@Nullable
	public Role getRole(@Nullable Player other) {
		if (equals(other) || other == null || other.isSpectating() || (!alive && game.getSettings().revealDeadRoles())) return role;

		if (other.getModifiers().stream().anyMatch(m -> m.canSeeRole(other, this))) return role;
		if (other.getRole().canSeeRole(other, this) || teams.stream().anyMatch(t -> t.canSeeRole(other, this))) return role.getEffectiveRole(game);

		return null;
	}

	@NotNull
	public List<Team> getTeams(@Nullable Player other) {
		if (equals(other) || other == null || other.isSpectating() || (!alive && game.getSettings().revealDeadRoles())) return getTeams();

		List<Team> teams = new ArrayList<>();

		Role role = getRole(other);
		if (role != null) teams.addAll(role.getTeams());
		teams.addAll(this.teams.stream().filter(t -> t.isVisible(other, this)).filter(t -> !teams.contains(t)).toList());

		return teams;
	}

	@Nullable
	public Aura getAura(@Nullable Player other) {
		return Aura.of(getTeams(other))
				.orElse(other != null && other.getRole().canSeeAura(other, this)
						? role.getEffectiveAura(game)
						: null
				);
	}

	@NotNull
	public Stream<? extends IPlayerModifier> getBehavior() {
		return Stream.concat(
				Stream.concat(
						getTeams().stream(),
						getModifiers().stream()
				),
				Stream.of(role)
		);
	}

	public void setRole(@NotNull Role role) {
		this.role = role;
	}

	public void setAlive(boolean alive, @Nullable KillReason reason) {
		if (alive && !this.alive) revive();
		if (!alive && this.alive) kill(reason != null ? reason : KillReason.UNKNOWN);
	}

	public void revive() {
		this.alive = true;
	}

	public void kill(@NotNull KillReason reason) {
		game.scheduleNightAction(() -> {
			game.getInteractions().remove(id);
			modifiers.removeIf(m -> !m.isPersistent());

			//This can NOT be replaced with "anyMatch" because handleDeath has to be executed for all modifiers!
			if (getBehavior().filter(b -> !b.handleDeath(this, reason)).count() > 0) return;

			sendEvent(EventType.KILL, reason);
			game.playSound(Sound.DEATH);

			game.pushProtocol(ProtocolEntry.ProtocolType.DEATH, new Object[] { id, reason });

			this.alive = false;
		});
	}

	public void sendUpdate() {
		sendEvent(EventType.UPDATE, GameState.create(game, this));
	}

	public void sendEvent(@NotNull EventType type, @NotNull Object data) {
		clients.removeIf(client -> {
			try {
				client.send(new EventPayload(type, data));
			} catch (Exception e) {
				if (e.getCause() instanceof ClosedChannelException) return true;
				logger.error("Failed to send event to {}", this, e);
			}

			return false;
		});
	}

	public void playSound(@NotNull Sound sound) {
		playSound(sound, 1);
	}

	public void playSound(@NotNull Sound sound, double volume) {
		sendEvent(EventType.SOUND, sound.data(volume));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Player p && p.getId().equals(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return name + " (" + id + ")";
	}

	private record EventPayload(EventType type, Object data) {
	}
}
