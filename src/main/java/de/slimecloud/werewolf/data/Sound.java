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
	LOVE,

	/**
	 * Played when it is the current player's turn to play
	 */
	ACTIVE_TURN,

	/**
	 * Played when it's the WereSlime's turn
	 */
	HOWL,

	/**
	 * Played when Player wins the game
	 */
	WIN;

	public Data data(double volume) {
		if (volume < 0 || volume > 1) throw new IllegalArgumentException("Volume should be between 0 and 1. Volume " + volume + " is out of range");
		return new Data(this, volume);
	}

	public record Data(@NotNull Sound sound, double volume) {

	}
}
