package de.slimecloud.werewolf.main.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class VoiceUpdateListener extends ListenerAdapter {
	private final DiscordBot bot;

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		if (event.getChannelJoined() == null) return;

		AtomicBoolean muted = new AtomicBoolean(event.getVoiceState().isGuildMuted());
		AtomicBoolean deafened = new AtomicBoolean(event.getVoiceState().isDeafened());

		if ((muted.get() || deafened.get()) && event.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
			List<AuditLogEntry> entries = event.getGuild().retrieveAuditLogs()
					.type(ActionType.MEMBER_UPDATE)
					.order(PaginationAction.PaginationOrder.BACKWARD)
					.limit(50).stream()
					.filter(e -> e.getTargetIdLong() == event.getMember().getIdLong())
					.filter(e -> e.getChanges().containsKey(AuditLogKey.MEMBER_MUTE.getKey()) || e.getChanges().containsKey(AuditLogKey.MEMBER_DEAF.getKey()))
					.toList();

			if (muted.get() && entries.stream()
					.filter(e -> e.getChangeByKey(AuditLogKey.MEMBER_MUTE) != null)
					.findFirst().filter(e -> e.getUserIdLong() == event.getGuild().getSelfMember().getIdLong()).isPresent()
			) muted.set(false);

			if (deafened.get() && entries.stream()
					.filter(e -> e.getChangeByKey(AuditLogKey.MEMBER_DEAF) != null)
					.findFirst().filter(e -> e.getUserIdLong() == event.getGuild().getSelfMember().getIdLong()).isPresent()
			) deafened.set(false);
		}

		bot.getGame(event.getChannelJoined().getId()).ifPresentOrElse(
				game -> {
					if (muted.get() || deafened.get()) event.getGuild().kickVoiceMember(event.getMember()).queue();
					else game.updateVoiceMember(event.getMember()).queue();
				},
				() -> DiscordBot.updateMute(event.getVoiceState(), muted.get(), deafened.get()).queue()
		);
	}
}
