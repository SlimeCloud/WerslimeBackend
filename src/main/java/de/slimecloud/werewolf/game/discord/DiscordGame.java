package de.slimecloud.werewolf.game.discord;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.Role;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.Result;
import net.dv8tion.jda.internal.requests.CompletedRestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
public class DiscordGame extends Game {
	private final long guild;

	@Setter
	private Runnable cleanup;

	public DiscordGame(@NotNull Main main, @NotNull VoiceChannel channel) {
		super(main, channel.getId());

		this.guild = channel.getGuild().getIdLong();

		updateVoice();
	}

	@NotNull
	public Optional<Guild> getGuild() {
		return Optional.ofNullable(main.getBot().getJda().getGuildById(guild));
	}

	@NotNull
	public Optional<VoiceChannel> getChannel() {
		return Optional.ofNullable(main.getBot().getJda().getVoiceChannelById(id));
	}

	@NotNull
	@Override
	public Player join(@NotNull String name) {
		throw new ErrorResponse(ErrorResponseType.INVALID_GAME_STATE);
	}

	@NotNull
	public DiscordPlayer join(@NotNull String id, @NotNull String name) {
		DiscordPlayer player = new DiscordPlayer(this, id, name);

		if (isStarted()) {
			player.setRole(Role.VILLAGER);
			player.setAlive(false, null);
		}

		players.put(player.getId(), player);
		sendUpdate();

		updateVoice();

		return player;
	}

	@Nullable
	@Override
	public DiscordPlayer leave(@NotNull String player, @Nullable Consumer<Player> event) {
		DiscordPlayer temp = (DiscordPlayer) super.leave(player, event);
		updateVoice();

		getGuild().map(g -> g.getMemberById(player)).map(Member::getVoiceState).ifPresent(s -> s.declineSpeaker().queue());

		return temp;
	}

	@Override
	public void reset() {
		super.reset();
		updateVoice();
	}

	@Override
	public void start() {
		super.start();
		updateVoice();
	}

	@Override
	public void next() {
		super.next();
		updateVoice();
	}

	public void updateVoice() {
		getChannel().ifPresent(channel -> {
			List<RestAction<Result<Void>>> actions = channel.getMembers().stream()
					.map(this::updateVoiceMember)
					.map(RestAction::mapToResult)
					.toList();

			if (!actions.isEmpty()) RestAction.allOf(actions).queue();
		});
	}

	@NotNull
	public RestAction<Void> updateVoiceMember(@NotNull Member member) {
		GuildVoiceState state = member.getVoiceState();
		Player player = players.get(member.getUser().getId());

		RestAction<Void> action = state != null ? settings.muteSystem().mute(this, state, player) : null;
		return action != null ? action : new CompletedRestAction<>(member.getJDA(), null);
	}

	@Override
	public void cleanup() {
		started = false;
		updateVoice();

		if (cleanup != null) cleanup.run();
		super.cleanup();
	}
}
