package de.slimecloud.werewolf.game;

import de.slimecloud.werewolf.data.MuteSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Accessors(fluent = true)
@AllArgsConstructor
public class GameSettings {
	@NotNull
	public static GameSettings createDefault() {
		return new GameSettings(2, MuteSystem.NONE, Arrays.asList(Role.WITCH, Role.AMOR, Role.SEER), false, true, true, false, false, true);
	}

	private Integer werewolfAmount;
	private MuteSystem muteSystem;
	private List<Role> roles;

	private Boolean isPublic;
	private Boolean revealDeadRoles;
	private Boolean deadSpectators;
	private Boolean revealLoverRoles;
	private Boolean storyMode;
	private Boolean chat;

	@SuppressWarnings("unchecked")
	public static boolean validate(@NotNull String name, @NotNull Object value) {
		return switch (name) {
			case "werewolfAmount" -> (int) value > 0;
			case "roles" -> ((List<Role>) value).stream().noneMatch(Objects::isNull);
			default -> true;
		};
	}
}
