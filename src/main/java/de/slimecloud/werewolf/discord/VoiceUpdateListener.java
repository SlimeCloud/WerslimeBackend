package de.slimecloud.werewolf.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class VoiceUpdateListener extends ListenerAdapter {
	private final DiscordBot bot;

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		if (event.getChannelJoined() != null) bot.getGame(event.getChannelJoined().getIdLong()).ifPresent(game -> {
			if (event.getVoiceState().isGuildMuted()) event.getGuild().kickVoiceMember(event.getMember()).queue();
			else event.getMember().mute(game.shouldMute(event.getMember())).queue();
		});
	}
}
