package de.slimecloud.werewolf.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public enum Modifier implements IPlayerModifier {
	MAYOR(false) {
		@Override
		public double getVoteMultiplier(@NotNull Player player) {
			return player.getGame().getCurrent() == Role.VILLAGER ? 1.5 : 1;
		}
	},
	LOVER(true) {
		@Override
		public boolean isVisible(@Nullable Player player) {
			return player != null && player.hasTeam(Team.LOVER);
		}

		@Override
		public boolean canSeeAura(@NotNull Player player, @NotNull Player target) {
			return player.hasModifier(LOVER) && target.hasModifier(LOVER);
		}
	},
	SHIELD(false) {
		@Override
		public boolean isVisible(@Nullable Player player) {
			return player != null && player.getRole() == Role.HEALER;
		}
	};

	private final boolean persistent;

	public boolean isVisible(@Nullable Player player) {
		return true;
	}
}
