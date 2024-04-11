package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Game {
	private final UUID id = UUID.randomUUID();
	private final Map<UUID, Player> players = new HashMap<>();
	private final UUID master;
	private final UUID mayor;
	private final boolean started = false;
	@Setter
	private GameSettings settings = new GameSettings();
}
