package de.slimecloud.werewolf.game;

import de.slimecloud.werewolf.data.KillReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Order is win priority
 */
@Getter
@AllArgsConstructor
public enum Team implements IPlayerModifier {
	JESTER(Aura.NEUTRAL),
	LOVER(Aura.NEUTRAL) {
		@Override
		public boolean handleDeath(@NotNull Player player, @NotNull KillReason reason) {
			if (reason != KillReason.LOVER && player.hasModifier(Modifier.LOVER)) {
				player.getGame().getPlayers()
						.filter(p -> p.hasModifier(Modifier.LOVER))
						.forEach(p -> p.kill(KillReason.LOVER));
			}

			return true;
		}

		@Override
		public boolean isWinning(@NotNull Game game) {
			return game.getPlayers().findAny().isPresent() && game.getPlayers().filter(Player::isAlive).allMatch(p -> p.hasTeam(LOVER));
		}

		@Override
		public boolean canSeeRole(@NotNull Player player, @NotNull Player target) {
			return player.hasModifier(Modifier.LOVER) && target.hasModifier(Modifier.LOVER) && player.getGame().getSettings().revealLoverRoles();
		}

		@Override
		public boolean isVisible(@NotNull Player player, @NotNull Player target) {
			return false;
		}
	},
	WEREWOLF(Aura.HOSTILE) {
		@Override
		public boolean isWinning(@NotNull Game game) {
			int werewolves = getPlayerCount(game, p -> p.hasTeam(WEREWOLF));
			int other = getPlayerCount(game, p -> !p.hasTeam(WEREWOLF));

			return werewolves >= other;
		}

		@Override
		public boolean isVisible(@NotNull Player player, @NotNull Player target) {
			return super.isVisible(player, target) && player.getRole() != Role.WARLOCK && target.getRole() != Role.WARLOCK;
		}
	},
	VILLAGE(Aura.VILLAGE) {
		@Override
		public boolean isWinning(@NotNull Game game) {
			int hostile = getPlayerCount(game, p -> p.hasFlag(RoleFlag.KILLING));
			return hostile == 0;
		}

		@Override
		public boolean isVisible(@NotNull Player player, @NotNull Player target) {
			return false;
		}
	};

	private final Aura aura;

	public boolean isWinning(@NotNull Game game) {
		return false;
	}

	public boolean isVisible(@NotNull Player player, @NotNull Player target) {
		return player.hasTeam(this) && target.hasTeam(this);
	}

	private static int getPlayerCount(@NotNull Game game, @NotNull Predicate<Player> condition) {
		return (int) game.getPlayers().filter(Player::isAlive).filter(condition).count();
	}
}
