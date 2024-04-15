package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final Map<String, PlayerInfo> players;

	private final boolean started;
	private final GameSettings settings;

	private final Role current;
	private final String victim;

	private final Map<String, String> votes;


	@NotNull
	public static GameInfo create(@NotNull Game game, @Nullable Player self) {
		return new GameInfo(
				game.getId().toString(),
				game.getPlayers().values().stream().map(p ->
						PlayerInfo.create(p,
								!p.isAlive() ||
										(self != null && (
												p.getId().equals(self.getId()) ||
														(self.getRole() == Role.SEER && game.getSeerVisible().contains(p.getId().toString())) ||
														(self.getRole() == Role.WEREWOLF && p.getRole() == Role.WEREWOLF)
										))
						)
				).collect(Collectors.toMap(PlayerInfo::getId, p -> p)),
				game.isStarted(),
				game.getSettings(),
				game.getCurrent(),
				self != null && (self.getRole() == Role.WITCH || self.getRole() == Role.WEREWOLF) ? game.getVictim() : null,
				game.getVotes()
		);
	}
}
