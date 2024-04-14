package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class GameState {
	public final GameInfo game;
	public final PlayerInfo player;

	@NotNull
	public static GameState create(@NotNull Game game, @NotNull Player player) {
		return new GameState(GameInfo.create(game, player.getId()), PlayerInfo.create(player, true));
	}
}