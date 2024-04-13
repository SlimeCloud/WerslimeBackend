package de.slimecloud.werewolf.api;

import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorizationInfo {
	private final Player player;
	private final Game game;
}
