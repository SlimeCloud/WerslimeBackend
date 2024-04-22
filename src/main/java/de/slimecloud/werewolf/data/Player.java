package de.slimecloud.werewolf.data;

import de.mineking.javautils.ID;
import io.javalin.http.sse.SseClient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

@Setter
@Getter
@RequiredArgsConstructor
public class Player {
	private final String id = ID.generate().asString();
	private final String name;
	private boolean master = false;

	private Role role = null;

	private boolean mayor = false;
	@Setter(AccessLevel.NONE)
	private boolean alive = false;

	private SseClient client = null;

	public boolean canSeeRole(@NotNull Game game, @NotNull Player player) {
		if (equals(player)) return true;
		if (role == Role.WEREWOLF && role == player.getRole()) return true;

		if (!player.isAlive() && game.getSettings().isRevealDeadRoles()) return true;
		if (this.role == Role.SEER) return game.getRoleMetaData(Role.SEER, HashSet::new).contains(player.getId());

		return false;
	}

	public void setAlive(@NotNull Game game, boolean alive) {
		if (alive && !this.alive) revive(game);
		if (!alive && this.alive) kill(game);
	}

	public void revive(@NotNull Game game) {
		this.alive = true;
	}

	public void kill(@NotNull Game game) {
		game.getInteractions().remove(id);
		mayor = false;

		if (role == Role.HUNTER) game.setCurrent(Role.HUNTER);
		else sendEvent("KILL", new Object());

		this.alive = false;
	}

	public void sendUpdate(@NotNull Game game) {
		sendEvent("UPDATE", GameState.create(game, this));
	}

	public void sendEvent(@NotNull String name, @NotNull Object data) {
		if (client != null) client.sendEvent(name, data, UUID.randomUUID().toString());
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Player p && p.getId().equals(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
