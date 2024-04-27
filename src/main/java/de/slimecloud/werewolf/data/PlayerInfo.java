package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String avatar;

	private final String name;
	private final Role role;
	private final Team team;

	private final boolean master;
	private final boolean alive;
	private final boolean mayor;
	private final boolean lover;

	private final boolean connected;

	@NotNull
	public static PlayerInfo create(@NotNull Player player, boolean lover, boolean role, boolean team) {
		return new PlayerInfo(
				player.getId(),
				player.getAvatar(),

				player.getName(),
				role ? player.getRole() : null,
				team && player.getRole() != null ? player.getRole().getTeam() : null,

				player.isMaster(),
				player.isAlive(),
				player.isMayor(),
				lover && player.isLover(),

				!player.getClients().isEmpty()
		);
	}
}
