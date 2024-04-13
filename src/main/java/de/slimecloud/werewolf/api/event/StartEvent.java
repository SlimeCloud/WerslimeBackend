package de.slimecloud.werewolf.api.event;

import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Role;
import org.jetbrains.annotations.NotNull;

public class StartEvent extends Event {
	public final Role role;

	public StartEvent(@NotNull Game game, @NotNull Role role) {
		super(game);
		this.role = role;
	}
}
