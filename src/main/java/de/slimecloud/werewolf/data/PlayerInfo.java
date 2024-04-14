package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String name;

	private final boolean master;
	private final Role role;

	private final boolean alive;
	private final boolean mayor;

	@NotNull
	public static PlayerInfo create(@NotNull Player player, boolean role) {
		return new PlayerInfo(
				player.getId().toString(),
				player.getName(),
				player.isMaster(),
				role ? player.getRole() : null,
				player.isAlive(),
				player.isMayor()
		);
	}
}
