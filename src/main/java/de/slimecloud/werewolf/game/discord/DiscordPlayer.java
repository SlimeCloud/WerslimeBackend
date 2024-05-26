package de.slimecloud.werewolf.game.discord;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.game.Player;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiscordPlayer extends Player {
	public DiscordPlayer(@NotNull DiscordGame game, @NotNull String id, @NotNull String name) {
		super(game, id, name);
	}

	@Override
	public DiscordGame getGame() {
		return (DiscordGame) super.getGame();
	}

	@Nullable
	@Override
	public String getAvatar() {
		return getGame().getGuild().map(g -> g.getMemberById(id)).map(Member::getEffectiveAvatarUrl).orElse(null);
	}

	@Override
	public Player setName(@NotNull String name) {
		throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);
	}

	@NotNull
	@Override
	public String getName() {
		return getGame().getGuild().map(g -> g.getMemberById(id)).map(Member::getEffectiveName).orElse(name);
	}
}
