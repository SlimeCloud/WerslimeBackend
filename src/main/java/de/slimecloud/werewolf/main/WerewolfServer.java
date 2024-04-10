package de.slimecloud.werewolf.main;

import de.slimecloud.werewolf.api.APIServer;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class WerewolfServer {
	private final Config config;
	private final Dotenv credentials;

	private final APIServer server;

	public WerewolfServer(@NotNull Config config, @NotNull Dotenv credentials) {
		this.config = config;
		this.credentials = credentials;

		this.server = new APIServer(this);
	}
}
