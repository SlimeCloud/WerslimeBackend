package de.slimecloud.werewolf.discord.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.ICommandPermission;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.werewolf.discord.DiscordBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "create", description = "Erstellt eine neue Discord-Runde und sendet eine Einladung")
public class CreateCommand {
	public final ICommandPermission<ICommandContext> condition = new ICommandPermission<>() {
		@NotNull
		@Override
		public DefaultMemberPermissions requiredPermissions() {
			return DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER);
		}
	};

	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event, @NotNull DiscordBot bot,
	                           @Option(description = "Der Kanal, in dem gespielt wird", channelTypes = ChannelType.STAGE) StageChannel channel,
                               @Option(description = "Der Titel der Nachricht") String title,
                               @Option(description = "Die Rolle, die benachrichtigt wird", required = false) Role ping
	) {
		System.out.println(channel.getClass());
	}
}
