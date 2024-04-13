package de.slimecloud.werewolf.api.event;

import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.GameInfo;
import org.jetbrains.annotations.NotNull;

public class Event {
	public final GameInfo game;

	public Event(@NotNull Game game) {
		this.game = GameInfo.create(game);
	}
}
