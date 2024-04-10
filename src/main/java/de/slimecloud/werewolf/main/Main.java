package de.slimecloud.werewolf.main;

import com.google.gson.Gson;
import de.slimecloud.werewolf.api.Authenticator;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class Main {
	public final static Gson json = new Gson();

	public static void main(String[] args) throws Exception {
		new Main(Config.readFromFile("config"), Dotenv.configure().filename("credentials").load());
	}

	private final Map<String, Game> games = new HashMap<>();

	private final Config config;
	private final Dotenv credentials;

	private final Authenticator authenticator;
	private final Server server;

	public Main(Config config, Dotenv credentials) {
		this.config = config;
		this.credentials = credentials;

		this.authenticator = new Authenticator(this);
		this.server = new Server(this);

		this.server.start();
	}

	public void stop() {
		this.server.stop();
	}
}