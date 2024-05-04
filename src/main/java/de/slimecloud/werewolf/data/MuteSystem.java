package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.discord.DiscordBot;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MuteSystem {
	NONE {
		@Override
		@Nullable
		public RestAction<Void> mute(@NotNull Game game, @NotNull GuildVoiceState member, @Nullable Player player) {
			return null;
		}
	},
	FULL {
		@NotNull
		@Override
		public RestAction<Void> mute(@NotNull Game game, @NotNull GuildVoiceState member, @Nullable Player player) {
			return DiscordBot.updateMute(member,
					game.isStarted() && (player == null || (game.getCurrent().isDay() ^ player.isAlive())),
					game.isStarted() && (player != null && (!game.getCurrent().isDay() && player.isAlive()))
			);
		}
	},
	DEAD_ONLY {
		@NotNull
		@Override
		public RestAction<Void> mute(@NotNull Game game, @NotNull GuildVoiceState member, @Nullable Player player) {
			return DiscordBot.updateMute(member, player == null || !player.isAlive(), null);
		}
	};

	@Nullable
	public abstract RestAction<Void> mute(@NotNull Game game, @NotNull GuildVoiceState member, @NotNull Player player);
}
