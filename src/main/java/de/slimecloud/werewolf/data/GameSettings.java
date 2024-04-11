package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class GameSettings {
	private final int werewolfAmount = 2;
	private final List<String> roles = Stream.of(Role.values()).map(Role::name).toList();
}
