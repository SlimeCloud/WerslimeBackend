package de.slimecloud.werewolf.data;

import io.javalin.websocket.WsContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

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

	public boolean canSpeak() {
		return !game.getSettings().isMuteMembers() || !game.started || (alive && (game.getCurrent() == Role.VILLAGER || game.getCurrent() == Role.VILLAGER_ELECT));
	}

	public boolean canSeeRole(@NotNull Player player) {
		if (equals(player)) return true;
		if (isLover() && player.isLover() && game.getSettings().isRevealLoverRoles()) return true;
		if (role == Role.WEREWOLF && role == player.getRole()) return true;

		if (!player.isAlive() && game.getSettings().isRevealDeadRoles()) return true;
		if (this.role == Role.SEER) return game.<Set<String>>getRoleMetaData(Role.SEER).contains(player.getId());

		return false;
	}

	public boolean canSeeTeam(@NotNull Player player) {
		if (canSeeRole(player)) return true;
		if (this.role == Role.AURA_SEER) return game.<Set<String>>getRoleMetaData(Role.AURA_SEER).contains(player.getId());

		return false;
	}

	public void setAlive(boolean alive, @Nullable KillReason reason) {
		if (alive && !this.alive) revive();
		if (!alive && this.alive) kill(reason != null ? reason : KillReason.UNKNOWN);
	}

	public void revive() {
		this.alive = true;
	}

	public void kill(@NotNull KillReason reason) {
		game.getInteractions().remove(id);
		mayor = false;

		if (role == Role.HUNTER) game.setCurrent(Role.HUNTER);
		else if (role == Role.JESTER && reason == KillReason.VILLAGE_VOTE) game.sendWin(Winner.JESTER);
		else sendEvent("KILL", new Object());

		if (lover && reason != KillReason.LOVER) game.getPlayers().values().forEach(p -> {
			if (p.isLover()) p.kill(KillReason.LOVER);
		});

		this.alive = false;
	}

	public void sendUpdate() {
		sendEvent("UPDATE", GameState.create(game, this));
	}

	public void sendEvent(@NotNull String name, @NotNull Object data) {
		clients.forEach(client -> client.send(new EventPayload(name, data)));
	}

	public void playSound(@NotNull Sound sound, float volume) {
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

	private record EventPayload(String name, Object data) {
	}
}
