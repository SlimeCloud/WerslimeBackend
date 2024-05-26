package de.slimecloud.werewolf.api;

import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorizationInfo {
	private final Player player;
	private final Game game;
}
