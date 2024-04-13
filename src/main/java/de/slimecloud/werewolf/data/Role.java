package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	VILLAGER(false, 0),
	WEREWOLF(false, 0),
	WITCH(true, 3),
	HUNTER(true, 1),
	SEER(true, 2);

	private final boolean generate;
	private final int priority;
}