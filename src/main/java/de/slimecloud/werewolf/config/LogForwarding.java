package de.slimecloud.werewolf.config;

import de.mineking.discordutils.console.RedirectTarget;
import de.slimecloud.werewolf.discord.DiscordBot;
import org.jetbrains.annotations.NotNull;

public record LogForwarding(Type type, String id) {
	enum Type {
		USER {
			@NotNull
			@Override
			public RedirectTarget<DiscordBot> build(@NotNull String id) {
				return RedirectTarget.directMessage(Long.parseLong(id));
			}
		},
		CHANNEL {
			@NotNull
			@Override
			public RedirectTarget<DiscordBot> build(@NotNull String id) {
				return RedirectTarget.channel(Long.parseLong(id));
			}
		},
		WEBHOOK {
			@NotNull
			@Override
			public RedirectTarget<DiscordBot> build(@NotNull String value) {
				return RedirectTarget.webhook(value);
			}
		};

		@NotNull
		public abstract RedirectTarget<DiscordBot> build(@NotNull String value);
	}

	@NotNull
	public RedirectTarget<DiscordBot> build() {
		return type.build(id);
	}
}
