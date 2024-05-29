package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.game.*;
import de.slimecloud.werewolf.game.discord.DiscordGame;
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
		int total = (int) game.getPlayers().values().stream().filter(p -> p.isAlive() && game.getCurrent().hasRole(p)).count();
		Optional<Player> target = game.evaluateVote();

		return new GameInfo(
				game.getId(),
				game instanceof DiscordGame,
				game.getPlayers().values().stream().map(p -> PlayerInfo.create(p, self)).toList(),

				game.isStarted(),
				game.getSettings(),

				game.getCurrent(),
				self != null && self.canSeeVictim() ? game.getVictim() : null,

				self != null && game.getCurrent().canSeeInteractions(self) ? game.getInteractions() : null,
				self != null ? game.getRoleMetaData().get(self.getRole()) : null,

				self != null && game.getCurrent().canSeeInteractions(self) ? target.map(Player::getId).orElse(null) : null,
				interacted,
				total,
				interacted >= total && (!game.getCurrent().hasFlag(RoleFlag.VOTE) || target.isPresent())
		);
	}
}
