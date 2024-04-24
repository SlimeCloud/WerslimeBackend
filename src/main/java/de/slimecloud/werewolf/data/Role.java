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
	AMOR(Team.VILLAGE, false, false, 75) {
		@Getter
		public static class AmorRequest {
			private String first;
			private String second;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return game.getPlayers().values().stream().noneMatch(Player::isLover);
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			AmorRequest request = ctx.bodyValidator(AmorRequest.class)
					.check(r -> r.getFirst() != null, "Invalid 'first'")
					.check(r -> r.getSecond() != null, "Invalid 'second'")
					.get();

			Player first = game.getPlayers().get(request.getFirst());
			Player second = game.getPlayers().get(request.getSecond());

			if (first == null || !first.isAlive() || second == null || !second.isAlive()) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			first.setLover(true);
			second.setLover(true);
		}
	},
	SEER(Team.VILLAGE, false, false, 50) {
		@AllArgsConstructor
		public static class Response {
			private final Role role;
		}

		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>());
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return !game.<Set<String>>getRoleMetaData(this).containsAll(game.getPlayers().values().stream().filter(Player::isAlive).map(Player::getId).toList());
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			Player target = getTarget(game, ctx, Player::isAlive);

			if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
			if (!game.<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			ctx.json(new Response(target.getRole()));
		}
	},
	AURA_SEER(Team.VILLAGE, false, false, 5) {
		@AllArgsConstructor
		public static class Response {
			private final Team role;
		}

		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>());
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return !game.<Set<String>>getRoleMetaData(this).containsAll(game.getPlayers().values().stream().filter(Player::isAlive).map(Player::getId).toList());
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			Player target = getTarget(game, ctx, Player::isAlive);

			if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
			if (!game.<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			ctx.json(new Response(target.getRole().getTeam()));
		}
	},
	WEREWOLF(Team.WEREWOLF, true, false, 0) {
		@Override
		public boolean canSeeVictim(@NotNull Game game) {
			return true;
		}
	},
	WITCH(Team.VILLAGE, true, false, 100) {
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
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>(Arrays.asList(WitchAction.POISON, WitchAction.HEAL)));
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return !game.<Set<WitchAction>>getRoleMetaData(this).isEmpty();
		}

		@Override
		public boolean canSeeVictim(@NotNull Game game) {
			return true;
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			WitchAction action = ctx.bodyValidator(WitchRequest.class)
					.check(r -> r.getAction() != null, "Invalid 'action'")
					.get().getAction();

			Set<WitchAction> actions = game.getRoleMetaData(this);

			if (action == WitchAction.SKIP) return;
			if (!actions.contains(action)) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

			switch (action) {
				case HEAL -> game.setVictim(null);
				case POISON -> getTarget(game, ctx, Player::isAlive).kill(game, KillReason.WITCH_POISON);
			}

			actions.remove(action);
		}
	},
	VILLAGER(Team.VILLAGE, true, false, 0),
	HUNTER(Team.VILLAGE, false, true, 10) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			Player target = getTarget(game, ctx, Player::isAlive);

			if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			target.kill(game, KillReason.HUNTER);
		}
	},
	JESTER(Team.NEUTRAL, false, false, 2) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final Team team;
	private final boolean vote;
	private final boolean dead;
	private final int priority;

	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
		game.getInteractions().put(player.getId(), getTarget(game, ctx, Player::isAlive).getId());
	}

	public void initialize(@NotNull Game game) { }

	public boolean canUseRole(@NotNull Game game) {
		return true;
	}

	public boolean canSeeVictim(@NotNull Game game) {
		return false;
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