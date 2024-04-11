package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String name;

	private final boolean master;

	@NotNull
	public static PlayerInfo create(@NotNull Player player) {
		return new PlayerInfo(
				player.getId().toString(),
				player.getName(),
				player.isMaster()
		);
	}
}
