package de.slimecloud.werewolf.data.discord;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.data.Player;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
		return Optional.ofNullable(getGame().getGuild()).map(g -> g.getMemberById(getId())).map(Member::getEffectiveAvatarUrl).orElse(null);
	}

	@Override
	public Player setName(@NotNull String name) {
		throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);
	}

	@NotNull
	@Override
	public String getName() {
		return Optional.ofNullable(getGame().getGuild()).map(g -> g.getMemberById(getId())).map(Member::getEffectiveName).orElse(null);
	}
}
