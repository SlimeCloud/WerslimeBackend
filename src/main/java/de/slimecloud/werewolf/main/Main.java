package de.slimecloud.werewolf.main;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import de.mineking.javautils.ID;
import de.slimecloud.werewolf.api.Authenticator;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.config.Config;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
import de.slimecloud.werewolf.discord.DiscordBot;
import io.github.cdimascio.dotenv.Dotenv;
import io.mokulu.discord.oauth.DiscordOAuth;
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
	private final DiscordBot bot;
	private final DiscordOAuth oauth2;

	public Main(@NotNull Config config, @NotNull Dotenv credentials) {
		this.config = config;
		this.credentials = credentials;

		this.authenticator = new Authenticator(this);
		this.bot = new DiscordBot(this);
		this.oauth2 = new DiscordOAuth(credentials.get("DISCORD_ID"), credentials.get("DISCORD_SECRET"), config.getUrl() + "/oauth2", new String[] {"identify"});
		this.server = new Server(this);

		this.server.start();
	}

	@NotNull
	public Game create(@NotNull String name) {
		String id = ID.generate().asString();

		Game game = new Game(this, ID.generate().asString(), id);

		Player player = new Player(game, id, name);
		player.setMaster(true);

		game.getPlayers().put(player.getId(), player);
		games.put(game.getId(), game);

		return game;
	}

	public void stop() {
		this.server.stop();
	}
}