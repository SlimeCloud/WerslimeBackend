package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Game {
	private final UUID id;
	private final Set<Player> players = new HashSet<>();
}
