package de.slimecloud.werewolf.data;

import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.sequence.EditScript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

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

	protected Role role = null;

	protected boolean mayor = false;
	protected boolean lover;

	@Setter(AccessLevel.NONE)
	protected boolean alive = false;

	protected Set<WsContext> clients = new HashSet<>();

	@Nullable
	public String getAvatar() {
		return null;
	}

	public double voteCount(@NotNull Role current) {
		if (!current.hasRole(this)) return 0;
		if (current == Role.WEREWOLF && role == Role.SPY) return 0;

		return current == Role.VILLAGER && mayor ? 1.5 : 1;
	}

	public boolean canSeeRole(@NotNull Player player) {
		if (equals(player)) return true;
		if (isLover() && player.isLover() && game.getSettings().revealLoverRoles()) return true;

		if (!player.isAlive() && game.getSettings().revealDeadRoles()) return true;

		if (role == Role.HUNTER && game.current == Role.HUNTER) return false;
		if (!isAlive() && game.getSettings().deadSpectators()) return true;

		if (this.role == Role.SEER) return game.<Set<String>>getRoleMetaData(Role.SEER).contains(player.getId());

		return false;
	}

	public boolean canSeeTeam(@NotNull Player player) {
		if (canSeeRole(player)) return true;

		if (getEffectiveTeam(false) == Team.HOSTILE && player.getEffectiveTeam(false) == Team.HOSTILE) return true;

		if (!player.isAlive() && game.getSettings().revealDeadRoles()) return true;

		if (role == Role.HUNTER && game.current == Role.HUNTER) return false;
		if (!isAlive() && game.getSettings().deadSpectators()) return true;

		if (this.role == Role.AURA_SEER) return game.<Set<String>>getRoleMetaData(Role.AURA_SEER).contains(player.getId());

		return false;
	}

	@Nullable
	public Team getEffectiveTeam(boolean lover) {
		if (role == Role.SPY) return Team.HOSTILE;
		if (this.lover && lover) return Team.NEUTRAL;
		return role == null ? null : role.getTeam();
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
			mayor = false;

			if (role == Role.HUNTER) {
				game.getRoleMetaData().put(Role.HUNTER, game.getCurrent());
				game.setCurrent(Role.HUNTER);
			}
			else if (role == Role.JESTER && reason == KillReason.VILLAGE_VOTE) game.sendWin(Winner.JESTER);
			else sendEvent("KILL", new Object());

			if (role != Role.JESTER) game.playSound(Sound.DEATH);

			if (lover && reason != KillReason.LOVER) game.getPlayers().values().forEach(p -> {
				if (p.isLover()) p.kill(KillReason.LOVER);
			});

			this.alive = false;
		});
	}

	public void sendUpdate() {
		sendEvent("UPDATE", GameState.create(game, this));
	}

	public void sendEvent(@NotNull String name, @NotNull Object data) {
		clients.forEach(client -> {
			try {
				client.send(new EventPayload(name, data));
			} catch (Exception e) {
				logger.error("Failed to send event to {}", this, e);
			}
		});
	}

	public void playSound(@NotNull Sound sound) {
		playSound(sound, 1);
	}

	public void playSound(@NotNull Sound sound, double volume) {
		sendEvent("SOUND", sound.data(volume));
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
		return id + " (" + name + ")";
	}

	private record EventPayload(String name, Object data) {
	}
}
