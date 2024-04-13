package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameSettings {
	private final int werewolfAmount = 2;
	private final List<Role> roles = Arrays.stream(Role.values()).filter(Role::isGenerate).toList();
}
