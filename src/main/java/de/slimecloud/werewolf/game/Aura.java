package de.slimecloud.werewolf.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum Aura {
	NEUTRAL,
	HOSTILE,
	VILLAGE;

	@NotNull
	public static Optional<Aura> of(@NotNull List<Team> teams) {
		return teams.stream()
				.map(Team::getAura)
				.min(Comparator.comparing(Aura::ordinal));
	}
}
