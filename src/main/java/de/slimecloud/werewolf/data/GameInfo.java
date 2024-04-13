package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final List<PlayerInfo> players;
	private final String master;
	private final boolean started;
	private final GameSettings settings;


	@NotNull
	public static GameInfo create(@NotNull Game game, @Nullable UUID self) {
		return new GameInfo(
				game.getId().toString(),
				game.getPlayers().values().stream().map(p -> PlayerInfo.create(p, !p.isAlive() || p.getId().equals(self))).toList(),
				game.getMaster().toString(),
				game.isStarted(),
				game.getSettings()
		);
	}
}
