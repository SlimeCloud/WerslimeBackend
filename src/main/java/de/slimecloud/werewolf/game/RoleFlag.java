package de.slimecloud.werewolf.game;

public enum RoleFlag {
	/**
	 * Role has action at day
	 */
	DAY,

	/**
	 * Role is voting
	 */
	VOTE,

	/**
	 * Can see wh was the victim of the night
	 */
	VICTIM,

	/**
	 * Roel can kill
	 */
	KILLING
}
