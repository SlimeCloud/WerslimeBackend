package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.Role;
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
		return new GameState(GameInfo.create(game, player), PlayerInfo.create(player, true,
				player.isLover() || player.getRole() == Role.AMOR || (game.getSettings().deadSpectators() && !player.isAlive() && !(game.getCurrent() == Role.HUNTER && player.getRole() == Role.HUNTER)),
				true, true
		));
	}
}
