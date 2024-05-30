package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.RoleFlag;
import de.slimecloud.werewolf.main.discord.DiscordBot;
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
			if (game.getSettings().storyMode()) return DiscordBot.updateMute(member,
					player == null || (game.isStarted() && !player.isMaster() && (!player.isAlive() || !game.getCurrent().hasFlag(RoleFlag.DAY))),
					null
			);

			return DiscordBot.updateMute(member,
					game.isStarted() && (player == null || (game.getCurrent().hasFlag(RoleFlag.DAY) ^ player.isAlive())),
					game.isStarted() && (player != null && (!game.getCurrent().hasFlag(RoleFlag.DAY) && player.isAlive()))
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
