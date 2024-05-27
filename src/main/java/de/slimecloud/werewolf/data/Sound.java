package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public enum Sound {
	START,
	WIN,
	LOSE,

	DEATH,
	LOVE,

	ACTIVE_TURN,
	WARLOCK,
	HOWL,
	POTION,
	SEER,
	SHOOT,
	VILLAGER;

	private final int variantAmount;

	Sound() {
		this(1);
	}

	public Data data(double volume) {
		return data(volume, Main.random.nextInt(variantAmount));
	}

	public Data data(double volume, int variant) {
		if (volume < 0 || volume > 1) throw new IllegalArgumentException("Volume should be between 0 and 1. Volume " + volume + " is out of range");
		if (variantAmount > 1 && variant == -1) variant = 0;
		if (variantAmount == 1 && variant == 0) variant = -1;
		return new Data(this, volume, variant);
	}

	public record Data(@NotNull Sound sound, double volume, int variant) {

	}
}
