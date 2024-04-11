package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Player {
	private final String name;
	private final UUID id = UUID.randomUUID();

	private final boolean master;

	private final Role role = Role.getRandom();
}
