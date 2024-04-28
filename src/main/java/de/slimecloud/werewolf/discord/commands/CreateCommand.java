package de.slimecloud.werewolf.discord.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.ICommandPermission;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.slimecloud.werewolf.data.discord.DiscordGame;
import de.slimecloud.werewolf.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@ApplicationCommand(name = "create", description = "Erstellt eine neue Discord-Runde und sendet eine Einladung", scope = Scope.GUILD_GLOBAL)
public class CreateCommand {
	public final ICommandPermission<ICommandContext> condition = new ICommandPermission<>() {
		@NotNull
		@Override
		public DefaultMemberPermissions requiredPermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	};

	private final MessageMenu menu;

	public CreateCommand(@NotNull DiscordBot bot, @NotNull UIManager manager) {
		menu = manager.createMenu("game",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setTitle(s.getCache("title"))
						.setColor(Color.decode(bot.getMain().getConfig().getColor()))
						.setAuthor(s.<Member>getCache("master").getEffectiveName(), null, s.<Member>getCache("master").getEffectiveAvatarUrl())
						.setThumbnail(s.getEvent().getGuild().getIconUrl())
						.setDescription(
								"Spielt mit bei Werslime in " + s.<VoiceChannel>getCache("channel").getAsMention() + "! \n\n" +
										"Um der Runde beizutreten, [**verifiziere dich mit OAuth2**](" + bot.getMain().getConfig().getUrl() + "/game/" + s.getState("game", String.class) + ")" +
										" oder verwende den **Knopf unter dieser Nachricht**, um ohne OAuth2 beizutreten!"
						)
						.build()
				).withContent(s -> s.getCache("mention")),
				new ButtonComponent("join", ButtonColor.GREEN, "Beitreten").appendHandler(state -> {
					String id = state.getState("game", String.class);
					DiscordGame game = (DiscordGame) bot.getMain().getGames().getIfPresent(id);

					if (game == null) {
						state.getEvent().reply(":x: Diese Runde wurde beendet! Wenn du dennoch Werslime spielen möchtest, kannst du unter " + bot.getMain().getConfig().getUrl() + " deine eigene Runde erstellen!").setEphemeral(true).queue();
						return;
					}

					if (!game.getPlayers().containsKey(state.getEvent().getUser().getId())) game.join(state.getEvent().getUser().getId(), state.getEvent().getUser().getName());

					state.getEvent().reply(bot.getMain().getConfig().getUrl() + "/join?token=" + bot.getMain().getAuthenticator().generateToken(state.getEvent().getUser().getId(), id))
							.setEphemeral(true)
							.queue();
				})
		);
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull DiscordBot bot,
	                           @Option(description = "Der Kanal, in dem gespielt wird", channelTypes = ChannelType.VOICE) VoiceChannel channel,
	                           @Option(description = "Der Titel der Nachricht") String title,
	                           @Option(description = "Die Rolle, die benachrichtigt wird", required = false) Role ping
	) {
		if (bot.getGame(channel.getIdLong()).isPresent()) {
			event.reply(":x: In diesem Kanal läuft bereits ein Spiel!").setEphemeral(true).queue();
			return;
		}

		DiscordGame game = bot.createGame(channel, event.getMember());

		menu.createState()
				.setState("game", game.getId())
				.setCache("title", title)
				.setCache("mention", ping != null ? ping.getAsMention() : null)
				.setCache("master", event.getMember())
				.setCache("channel", channel)
				.display(event, false);
	}
}
