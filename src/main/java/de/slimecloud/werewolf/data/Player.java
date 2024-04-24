package de.slimecloud.werewolf.data;

import de.mineking.javautils.ID;
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
	private final String id = ID.generate().asString();
	private final String name;
	private boolean master = false;

	@Setter(AccessLevel.NONE)
	private Team team = null;
	private Role role = null;

	private boolean mayor = false;
	private boolean lover;

	@Setter(AccessLevel.NONE)
	private boolean alive = false;

	private Set<WsContext> clients = new HashSet<>();

	public void setRole(@Nullable Role role) {
		this.role = role;
		this.team = role != null ? role.getTeam() : null;
	}

	public boolean canSeeRole(@NotNull Game game, @NotNull Player player) {
		if (equals(player)) return true;
		if (isLover() && player.isLover()) return true;
		if (role == Role.WEREWOLF && role == player.getRole()) return true;

		if (!player.isAlive() && game.getSettings().isRevealDeadRoles()) return true;
		if (this.role == Role.SEER) return game.<Set<String>>getRoleMetaData(Role.SEER).contains(player.getId());

		return false;
	}

	public boolean canSeeTeam(@NotNull Game game, @NotNull Player player) {
		if(canSeeRole(game, player)) return true;
		if (this.role == Role.AURA_SEER) return game.<Set<String>>getRoleMetaData(Role.AURA_SEER).contains(player.getId());

		return false;
	}

	public void setAlive(@NotNull Game game, boolean alive, @Nullable KillReason reason) {
		if (alive && !this.alive) revive(game);
		if (!alive && this.alive) kill(game, reason != null ? reason : KillReason.UNKNOWN);
	}

	public void revive(@NotNull Game game) {
		this.alive = true;
	}

	public void kill(@NotNull Game game, @NotNull KillReason reason) {
		game.getInteractions().remove(id);
		mayor = false;

		if (role == Role.HUNTER) game.setCurrent(Role.HUNTER);
		else if(role == Role.JESTER && reason == KillReason.VILLAGE_VOTE) game.sendWin(Winner.JESTER);
		else sendEvent("KILL", new Object());

		if(lover && reason != KillReason.LOVER) game.getPlayers().values().forEach(p -> {
			if(p.isLover()) p.kill(game, KillReason.LOVER);
		});

		this.alive = false;
	}

	public void sendUpdate(@NotNull Game game) {
		sendEvent("UPDATE", GameState.create(game, this));
	}

	public void sendEvent(@NotNull String name, @NotNull Object data) {
		clients.forEach(client -> client.send(new EventPayload(name, data)));
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Player p && p.getId().equals(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	private record EventPayload(String name, Object data) {}
}
