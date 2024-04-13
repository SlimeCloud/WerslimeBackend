package de.slimecloud.werewolf.api.event;

import de.slimecloud.werewolf.data.Game;
import org.jetbrains.annotations.NotNull;

public class Event {
	public final String type;
	public final Game game;

	public Event(@NotNull Game game, @NotNull String type) {
		this.game = game;
		this.type = type;
	}
}
