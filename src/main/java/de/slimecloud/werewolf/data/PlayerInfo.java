package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.game.Aura;
import de.slimecloud.werewolf.game.Modifier;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String avatar;

	private final String name;
	private final boolean master;

	private final boolean alive;
	private final boolean connected;

	private final Role role;
	private final Aura aura;
	private final Collection<Modifier> modifiers;

	@NotNull
	public static PlayerInfo create(@NotNull Player player, @Nullable Player viewer) {
		return new PlayerInfo(
				player.getId(),
				player.getAvatar(),

				player.getName(),
				player.isMaster(),

				player.isAlive(),
				!player.getClients().isEmpty(),

				player.getRole(viewer),
				player.getAura(viewer),
				player.getModifiers(viewer)
		);
	}
}
