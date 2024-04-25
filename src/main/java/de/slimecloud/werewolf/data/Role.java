package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum Role {
	VILLAGER_ELECT(Team.VILLAGE, true, false, false, 0) {
		@Override
		public boolean hasRole(@NotNull Game game, @NotNull Player player) {
			return true;
		}
	},
	AMOR(Team.VILLAGE, false, false, false, 75) {
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
	SEER(Team.VILLAGE, false, false, false, 50) {
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
	AURA_SEER(Team.VILLAGE, false, false, false, 5) {
		@AllArgsConstructor
		public static class Response {
			private final Team team;
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
	WEREWOLF(Team.WEREWOLF, true, true, false, 0) {
		@Override
		public boolean canSeeVictim(@NotNull Game game) {
			return true;
		}
	},
	WITCH(Team.VILLAGE, true, false, false, 100) {
		public enum WitchAction {
			POISON,
			HEAL
		}

		@Getter
		public static class WitchRequest {
			private Map<WitchAction, String> actions;
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
			Set<WitchAction> available = game.getRoleMetaData(this);
			Map<WitchAction, String> targets = ctx.bodyValidator(WitchRequest.class)
					.check(r -> r.getActions() != null, "Invalid 'action'")
					.get().getActions();

			Set<Runnable> execute = new HashSet<>();

			targets.forEach((action, target) -> {
				if(target == null) return;
				if(!available.contains(action)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				switch (action) {
					case HEAL -> {
						if(!target.equals(game.getVictim())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
						execute.add(() -> game.setVictim(null));
					}
					case POISON -> {
						Player p = getTarget(game, ctx, Player::isAlive);
						execute.add(() -> p.kill(game, KillReason.WITCH_POISON));
					}
				}

				available.remove(action);
			});

			execute.forEach(Runnable::run);
		}
	},
	VILLAGER(Team.VILLAGE, true, false, false, 0) {
		@Override
		public boolean hasRole(@NotNull Game game, @NotNull Player player) {
			return true;
		}
	},
	HUNTER(Team.VILLAGE, false, false, true, 10) {
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
	JESTER(Team.NEUTRAL, false, false, false, 2) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final Team team;
	private final boolean vote;
	private final boolean killing;
	private final boolean dead;
	private final int priority;

	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
		game.getInteractions().put(player.getId(), getTarget(game, ctx, Player::isAlive).getId());
	}

	public void initialize(@NotNull Game game) { }

	public boolean hasRole(@NotNull Game game, @NotNull Player player) {
		return player.getRole() == this;
	}

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