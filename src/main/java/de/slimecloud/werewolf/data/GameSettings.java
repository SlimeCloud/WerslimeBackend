package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GameSettings {
	private final int werewolfAmount = 2;
	private final Role[] roles = Role.values();
}
