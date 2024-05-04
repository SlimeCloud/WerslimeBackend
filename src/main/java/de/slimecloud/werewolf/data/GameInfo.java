package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.data.discord.DiscordGame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
public class GameInfo {
	private final String id;
	private final boolean discord;
	private final List<PlayerInfo> players;

	private final boolean started;
	private final GameSettings settings;

	private final Role current;
	private final String victim;

	private final Map<String, Object> interactions;
	private final Object roleMeta;

	private final String target;
	private final int interacted;
	private final int total;
	private final boolean valid;

	@NotNull
	public static GameInfo create(@NotNull Game game, @Nullable Player self) {
		int interacted = game.getInteractions().size();
		int total = (int) game.getPlayers().values().stream().filter(p -> (p.isAlive() || game.getCurrent().isDead()) && game.getCurrent().hasRole(p)).count();
		Optional<Player> target = game.evaluateVote();

		return new GameInfo(
				game.getId(),
				game instanceof DiscordGame,
				game.getPlayers().values().stream().map(p -> PlayerInfo.create(p,
						self != null && (self.isLover() || self.getRole() == Role.AMOR || (game.getSettings().deadSpectators() && !self.isAlive())),
						self != null && self.canSeeRole(p),
						self != null && self.canSeeTeam(p)
				)).toList(),

				game.isStarted(),
				game.getSettings(),

				game.getCurrent(),
				(self != null && self.getRole() != null) && (self.getRole().canSeeVictim(game) || (game.getSettings().deadSpectators() && !self.isAlive())) ? game.getVictim() : null,

				self != null && (game.getCurrent().canSeeInteractions(self) || (game.getSettings().deadSpectators() && !self.isAlive())) ? game.getInteractions() : null,
				self != null ? game.getRoleMetaData().get(self.getRole()) : null,

				target.map(Player::getId).orElse(null),
				interacted,
				total,
				interacted >= total && (!game.getCurrent().isVote() || target.isPresent())
		);
	}
}
