package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final List<PlayerInfo> players;
	private final String master;
	private final boolean started;
}
