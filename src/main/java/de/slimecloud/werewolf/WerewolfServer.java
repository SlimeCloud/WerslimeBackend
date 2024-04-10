package de.slimecloud.werewolf;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class WerewolfServer {
	private final Config config;
	private final Dotenv credentials;

	public WerewolfServer(@NotNull Config config, @NotNull Dotenv credentials) {
		this.config = config;
		this.credentials = credentials;
	}
}
