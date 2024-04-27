package de.slimecloud.werewolf.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Winner {
	LOVER(game -> game.getPlayers().values().stream().filter(Player::isAlive).filter(Player::isLover).count() >= game.getPlayers().values().stream().filter(Player::isAlive).count() && game.getPlayers().values().stream().filter(Player::isAlive).filter(p -> !p.isLover()).noneMatch(p -> p.getRole().isKilling())),
	WEREWOLF(game -> game.getPlayers().values().stream().filter(Player::isAlive).allMatch(p -> p.getRole().getTeam() == Team.HOSTILE)),
	VILLAGER(game -> game.getPlayers().values().stream().filter(Player::isAlive).noneMatch(p -> p.getRole().isKilling())),
	JESTER(game -> false); //Player#kill

	private final Predicate<Game> condition;

	public boolean isWinning(@NotNull Game game) {
		return condition.test(game);
	}
}
