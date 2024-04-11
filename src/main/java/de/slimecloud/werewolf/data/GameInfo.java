package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final List<PlayerInfo> players;
	private final String master;
	private final boolean started;

	@NotNull
	public static GameInfo create(@NotNull Game game) {
		return new GameInfo(
				game.getId().toString(),
				game.getPlayers().values().stream().map(PlayerInfo::create).toList(),
				game.getMaster().toString(),
				game.isStarted()
		);
	}
}
