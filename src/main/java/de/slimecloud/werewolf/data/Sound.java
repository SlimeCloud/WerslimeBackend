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

	public Data data(float volume) {
		return new Data(this, volume);
	}

	public record Data(@NotNull Sound sound, float volume) {

	}
}
