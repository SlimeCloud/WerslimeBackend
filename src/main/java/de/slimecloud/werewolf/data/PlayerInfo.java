package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String name;
	private final Role role;

	private final boolean master;
	private final boolean alive;
	private final boolean mayor;

	private final boolean connected;

	@NotNull
	public static PlayerInfo create(@NotNull Player player, boolean role) {
		return new PlayerInfo(
				player.getId(),
				player.getName(),
				role ? player.getRole() : null,

				player.isMaster(),
				player.isAlive(),
				player.isMayor(),

				!player.getClients().isEmpty()
		);
	}
}
