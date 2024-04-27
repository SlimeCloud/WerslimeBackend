package de.slimecloud.werewolf.data;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@RequiredArgsConstructor
public enum Winner {
	LOVER(Player::isLover, game -> game.getPlayers().values().stream().filter(Player::isAlive).filter(Player::isLover).count() >= game.getPlayers().values().stream().filter(Player::isAlive).count() && game.getPlayers().values().stream().filter(Player::isAlive).filter(p -> !p.isLover()).noneMatch(p -> p.getRole().isKilling())),
	WEREWOLF(Role.WEREWOLF::hasRole, game -> game.getPlayers().values().stream().filter(Player::isAlive).allMatch(p -> p.getRole().getTeam() == Team.HOSTILE)),
	VILLAGER(p -> !Role.WEREWOLF.hasRole(p), game -> game.getPlayers().values().stream().filter(Player::isAlive).noneMatch(p -> p.getRole().isKilling())),
	JESTER(Role.WEREWOLF::hasRole, game -> false); //Player#kill

	private final Predicate<Player> memberCondition;
	private final Predicate<Game> winCondition;

	public boolean isMember(@NotNull Player player) {
		return memberCondition.test(player);
	}

	public boolean isWinning(@NotNull Game game) {
		return winCondition.test(game);
	}
}
