package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameSettings {
	public final static GameSettings DEFAULT = new GameSettings(2, Arrays.asList(Role.WITCH, Role.SEER));

	private final int werewolfAmount;
	private final List<Role> roles;
}
