package de.slimecloud.werewolf.data;

import org.jetbrains.annotations.NotNull;

public enum Sound {
	/**
	 * Played on Death
	 */
	DEATH,
	/**
	 * Played when amor makes two people fall in love with each other
	 */
	LOVE;

	public Data data(float volume) {
		return new Data(this, volume);
	}

	public record Data(@NotNull Sound sound, float volume) {

	}
}
