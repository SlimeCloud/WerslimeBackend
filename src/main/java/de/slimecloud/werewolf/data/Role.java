package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum Role {
	SEER(false, false, 50) {
		@AllArgsConstructor
		public static class Response {
			private final Role role;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return !game.getRoleMetaData(this, HashSet::new).containsAll(game.getPlayers().values().stream().filter(Player::isAlive).toList());
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			Player target = getTarget(game, ctx, Player::isAlive);

			if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
			if (!game.getRoleMetaData(this, HashSet::new).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			ctx.json(new Response(target.getRole()));
		}
	},
	WEREWOLF(true, false, 0),
	WITCH(false, false, 100) {
		public enum WitchAction {
			POISON,
			HEAL,
			SKIP
		}

		@Getter
		public static class WitchRequest {
			private WitchAction action;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return !game.getRoleMetaData(this, () -> new HashSet<>(Arrays.asList(WitchAction.POISON, WitchAction.HEAL))).isEmpty();
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			WitchAction action = ctx.bodyValidator(WitchRequest.class)
					.check(r -> r.getAction() != null, "Invalid 'action'")
					.get().getAction();

			Set<WitchAction> actions = game.getRoleMetaData(this, () -> new HashSet<>(Arrays.asList(WitchAction.POISON, WitchAction.HEAL)));

			if (action == WitchAction.SKIP) return;
			if (!actions.contains(action)) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

			switch (action) {
				case HEAL -> game.setVictim(null);
				case POISON -> getTarget(game, ctx, Player::isAlive).kill(game);
			}

			actions.remove(action);
		}
	},
	VILLAGER(true, false, 0),
	HUNTER(false, true, 10) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			Player target = getTarget(game, ctx, Player::isAlive);

			if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			target.kill(game);
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final boolean vote;
	private final boolean dead;
	private final int priority;

	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
		game.getInteractions().put(player.getId(), getTarget(game, ctx, Player::isAlive).getId());
	}

	public boolean canUseRole(@NotNull Game game) {
		return true;
	}

	@NotNull
	private static Player getTarget(@NotNull Game game, @NotNull Context ctx, @Nullable Predicate<Player> condition) {
		String target = ctx.bodyValidator(TargetRequest.class)
				.check(t -> t.getTarget() != null, "Invalid 'target'")
				.get().getTarget();

		Player player = game.getPlayers().get(target);

		if (player == null) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
		if (condition != null && !condition.test(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

		return player;
	}

	@Getter
	private static class TargetRequest {
		private String target;
	}
}