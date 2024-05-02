package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class GameSettings {
	public final static GameSettings DEFAULT = new GameSettings(2, Arrays.asList(Role.WITCH, Role.AMOR, Role.SEER), false, true, true, false, true);

	private Integer werewolfAmount;
	private List<Role> roles;

	private Boolean isPublic;
	private Boolean revealDeadRoles;
	private Boolean deadSpectators;
	private Boolean revealLoverRoles;
	private Boolean muteMembers;

	@SuppressWarnings("unchecked")
	public static boolean validate(@NotNull String name, @NotNull Object value) {
		return switch (name) {
			case "werewolfAmount" -> (int) value > 0;
			case "roles" -> ((List<Role>) value).stream().noneMatch(Objects::isNull);
			default -> true;
		};
	}
}
