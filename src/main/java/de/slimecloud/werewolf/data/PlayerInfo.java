package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerInfo {
	private final String id;
	private final String name;

	private final boolean master;
}
