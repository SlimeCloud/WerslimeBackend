package de.slimecloud.werewolf.discord;

import de.mineking.discordutils.DiscordUtils;
import de.slimecloud.werewolf.config.ActivityConfig;
import de.slimecloud.werewolf.config.LogForwarding;
import de.slimecloud.werewolf.data.discord.DiscordGame;
import de.slimecloud.werewolf.data.discord.DiscordPlayer;
import de.slimecloud.werewolf.discord.commands.CreateCommand;
import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class DiscordBot extends ListenerAdapter {
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);
	private final Main main;

	private final JDA jda;
	private final DiscordUtils<DiscordBot> discordUtils;

	public DiscordBot(@NotNull Main main) {
		this.main = main;

		JDABuilder builder = JDABuilder.createDefault(main.getCredentials().get("DISCORD_TOKEN"))
				.setStatus(OnlineStatus.IDLE)
				.setActivity(Activity.customStatus("Bot startet..."))

				.enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableCache(CacheFlag.VOICE_STATE)

				.addEventListeners(this)
				.addEventListeners(new VoiceUpdateListener(this));

		discordUtils = DiscordUtils.create(builder, this)
				.mirrorConsole(main.getConfig().getLogForwarding().stream().map(LogForwarding::build).toList())
				.useEventManager(null)
				.useUIManager(null)
				.useCommandManager(e -> () -> e, e -> () -> e, manager -> {
					manager.registerCommand(CreateCommand.class);
					manager.updateCommands();
				})
				.build();


		jda = discordUtils.getJDA();
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		startActivity();
	}

	private void startActivity() {
		jda.getPresence().setStatus(OnlineStatus.ONLINE);

		List<Activity> activities = main.getConfig().getActivity().activities.stream()
				.map(ActivityConfig.ActivityEntry::build)
				.toList();

		executor.scheduleAtFixedRate(
				() -> jda.getPresence().setActivity(activities.get(Main.random.nextInt(activities.size()))),
				0, main.getConfig().getActivity().interval, TimeUnit.SECONDS
		);
	}

	@NotNull
	public DiscordGame createGame(@NotNull VoiceChannel channel, @NotNull Member user) {
		DiscordGame game = new DiscordGame(main, channel, user.getId());

		DiscordPlayer player = new DiscordPlayer(game, user.getId(), user.getUser().getName());
		player.setMaster(true);

		game.getPlayers().put(player.getId(), player);
		main.getGames().put(game.getId(), game);

		return game;
	}

	@NotNull
	public Optional<DiscordGame> getGame(long channel) {
		return main.getGames().asMap().values().stream()
				.filter(g -> g instanceof DiscordGame dg && channel == dg.getChannelId())
				.findAny()
				.map(g -> (DiscordGame) g);
	}
}
