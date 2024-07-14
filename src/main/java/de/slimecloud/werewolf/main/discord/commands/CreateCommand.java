package de.slimecloud.werewolf.main.discord.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.state.MessageSendState;
import de.slimecloud.werewolf.game.discord.DiscordGame;
import de.slimecloud.werewolf.main.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@ApplicationCommand(name = "create", description = "Erstellt eine neue Discord-Runde und sendet eine Einladung", scope = Scope.GUILD_GLOBAL)
public class CreateCommand {
	private final MessageMenu menu;
	private final MessageMenu deleteMenu;

	public CreateCommand(@NotNull DiscordBot bot, @NotNull UIManager manager) {
		menu = manager.createMenu("game",
				MessageRenderer.embed(s -> new EmbedBuilder()
						.setTitle(s.getCache("title"))
						.setColor(Color.decode(bot.getMain().getConfig().getColor()))
						.setAuthor(s.<Member>getCache("master").getEffectiveName(), null, s.<Member>getCache("master").getEffectiveAvatarUrl())
						.setThumbnail(s.getEvent().getGuild().getIconUrl())
						.setDescription("Spielt mit bei Werslime in " + s.<VoiceChannel>getCache("channel").getAsMention() + "! \n\n" +
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

					if (game.getPlayer(state.getEvent().getUser().getId()).isEmpty()) game.join(state.getEvent().getUser().getId(), state.getEvent().getUser().getName());

					state.getEvent().reply("## Einladungs-Link\nTeile diesen Link nicht mit anderen! Dieser bietet Zugriff auf deinen Werslime-Account für diese Runde!\n\n" + bot.getMain().getConfig().getUrl() + "/join?token=" + bot.getMain().getAuthenticator().generateToken(state.getEvent().getUser().getId(), id))
							.setEphemeral(true)
							.queue();
				})
		);

		deleteMenu = manager.createMenu("game.delete",
				MessageRenderer.content(
						":x: In diesem Kanal läuft bereits ein Spiel! Du kannst die bisherige Runde für diesen Kanal beenden und stattdessen eine neue erstellen. " +
						"Bedenke, dass durch das beenden alle Spieler aus der Runde entfernt werden und der neuen beitreten müssen. Diese Aktion kann nicht rückgängig gemacht werden!"
				),
				new ButtonComponent("delete", ButtonColor.RED, "Bisherige Runde beenden").appendHandler(s -> {
					bot.getMain().getGames().invalidate(s.getState("ch", String.class));

					if (s.getEvent() instanceof IMessageEditCallback edit) {
						edit.editMessage(":white_check_mark: Bisherige Runde beendet. Du kannst nun eine Runde für diesen Kanal erstellen…").setComponents().queue();
					}
				})
		);
	}

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull DiscordBot bot,
	                           @Option(description = "Der Kanal, in dem gespielt wird", channelTypes = ChannelType.VOICE) VoiceChannel channel,
	                           @Option(description = "Der Titel der Nachricht", maxLength = 50) String title,
	                           @Option(description = "Die Rolle, die benachrichtigt wird", required = false) Role ping
	) {
		boolean exists = bot.getGame(channel.getId()).isPresent();

		MessageSendState state = exists
				? deleteMenu.createState()
				: menu.createState();

		state.setCache("title", title)
				.setCache("mention", ping != null ? ping.getAsMention() : null)
				.setCache("master", event.getMember())
				.setCache("channel", channel)
				.setState("ch", channel.getId());

		if (exists) state.display(event);
		else createGame(bot, state, event);
	}

	private void createGame(@NotNull DiscordBot bot, @NotNull MessageSendState state, @NotNull IReplyCallback event) {
		DiscordGame game = bot.createGame(state.getCache("channel"), event.getMember());

		state.setState("game", game.getId()).display(event, false);
		event.getHook().retrieveOriginal().queue(message -> game.setCleanup(() -> message.editMessageComponents(ActionRow.of(Button.secondary("---", "Runde Beendet").asDisabled())).queue()));
	}
}
