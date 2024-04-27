package de.slimecloud.werewolf.data.discord;

import de.mineking.javautils.ID;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import de.slimecloud.werewolf.data.Role;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class DiscordGame extends Game {
	private final long guild;

	public DiscordGame(@NotNull Main main, @NotNull Guild guild, @NotNull String master) {
		super(main, ID.generate().asString(), master);
		this.guild = guild.getIdLong();
	}

	@Override
	public boolean isPublic() {
		return false; //TODO allow public games and verify using oauth2
	}

	@Nullable
	public Guild getGuild() {
		return getMain().getBot().getJda().getGuildById(guild);
	}

	@NotNull
	@Override
	public Player join(@NotNull String name) {
		throw new ErrorResponse(ErrorResponseType.INVALID_GAME_STATE);
	}

	public DiscordPlayer join(@NotNull Member user) {
		DiscordPlayer player = new DiscordPlayer(this, user.getId(), user.getUser().getName());

		if (isStarted()) {
			player.setRole(Role.VILLAGER);
			player.setAlive(false, null);
		}

		getPlayers().put(player.getId(), player);
		sendUpdate();

		return player;
	}
}
