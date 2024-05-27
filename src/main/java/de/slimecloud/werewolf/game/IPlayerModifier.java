package de.slimecloud.werewolf.game;

import de.slimecloud.werewolf.data.KillReason;
import org.jetbrains.annotations.NotNull;

public interface IPlayerModifier {
	default double getVoteMultiplier(@NotNull Player player) {
		return 1;
	}

	default boolean handleDeath(@NotNull Player player, @NotNull KillReason reason) {
		return true;
	}

	default boolean canSeeRole(@NotNull Player player, @NotNull Player target) {
		return false;
	}

	default boolean canSeeAura(@NotNull Player player, @NotNull Player target) {
		return false;
	}
}
