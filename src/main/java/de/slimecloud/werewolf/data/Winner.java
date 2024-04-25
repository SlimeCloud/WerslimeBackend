package de.slimecloud.werewolf.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Winner {
	LOVER(game -> game.getPlayers().values().stream().filter(Player::isAlive).allMatch(Player::isLover)),
	WEREWOLF(game -> game.getPlayers().values().stream().filter(Player::isAlive).allMatch(p -> p.getRole().getTeam() == Team.WEREWOLF)),
	VILLAGER(game -> game.getPlayers().values().stream().filter(Player::isAlive).noneMatch(p -> p.getRole().isSoloWin())),
	JESTER(game -> false);

	private final Predicate<Game> condition;

	public boolean isWinning(@NotNull Game game) {
		return condition.test(game);
	}
}
