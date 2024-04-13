package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	VILLAGER(false),
	WEREWOLF(false),
	WITCH(true),
	HUNTER(true),
	SEER(true);

	private final boolean generate;
}