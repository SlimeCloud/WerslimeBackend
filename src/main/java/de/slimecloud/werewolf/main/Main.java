package de.slimecloud.werewolf.main;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import de.slimecloud.werewolf.api.Authenticator;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
public class Main {
	public final static Gson json = new Gson();
	public final static Random random = new Random();

	public static void main(String[] args) throws Exception {
		new Main(Config.readFromFile("config"), Dotenv.configure().filename("credentials").load());
	}

	private final Cache<String, Game> games = Caffeine.newBuilder()
			.expireAfterAccess(4, TimeUnit.HOURS)
			.build();

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

	@NotNull
	public Game create(@NotNull String name) {
		Player player = new Player(name);
		player.setMaster(true);

		Game game = new Game(this, player.getId());

		game.getPlayers().put(player.getId(), player);
		games.put(game.getId(), game);

		return game;
	}

	public void stop() {
		this.server.stop();
	}
}