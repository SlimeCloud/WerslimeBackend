package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final List<PlayerInfo> players;

	private final boolean started;
	private final GameSettings settings;

	private final Role current;
	private final String victim;

	private final Map<String, Object> interactions;
	private final int interacted;
	private final int total;

	private final Object roleMeta;

	@NotNull
	public static GameInfo create(@NotNull Game game, @Nullable Player self) {
		return new GameInfo(
				game.getId(),
				game.getPlayers().values().stream().map(p -> PlayerInfo.create(p,
						self != null && (self.isLover() || self.getRole() == Role.AMOR || (game.getSettings().isDeadSpectators() && !self.isAlive())),
						self != null && (self.canSeeRole(game, p) || (game.getSettings().isDeadSpectators() && !self.isAlive())),
						self != null && (self.canSeeTeam(game, p) || (game.getSettings().isDeadSpectators() && !self.isAlive()))
				)).toList(),
				game.isStarted(),
				game.getSettings(),
				game.getCurrent(),
				(self != null && self.getRole() != null) && (self.getRole().canSeeVictim(game) || (game.getSettings().isDeadSpectators() && !self.isAlive())) ? game.getVictim() : null,
				game.getCurrent() == Role.VILLAGER || (self != null && (self.getRole() == game.getCurrent() || (game.getSettings().isDeadSpectators() && !self.isAlive()))) ? game.getInteractions() : null,
				game.getInteractions().size(),
				game.getCurrent() == Role.VILLAGER ? game.getPlayerCount() : (int) game.getPlayers().values().stream().filter(p -> (p.isAlive() || game.getCurrent().isDead()) && p.getRole() == game.getCurrent()).count(),
				self != null ? game.getRoleMetaData().get(self.getRole()) : null
		);
	}
}
