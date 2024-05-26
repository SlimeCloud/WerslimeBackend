package de.slimecloud.werewolf.main.config;

import net.dv8tion.jda.api.entities.Activity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActivityConfig {
	public Integer interval;

	public List<ActivityEntry> activities;

	public record ActivityEntry(@NotNull Activity.ActivityType type, @NotNull String text) {
		@NotNull
		public Activity build() {
			return Activity.of(type, text);
		}
	}
}
