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
	AMOR(Team.VILLAGE, false, false, false, false, 75) {
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
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			AmorRequest request = ctx.bodyValidator(AmorRequest.class)
					.check(r -> r.getFirst() != null, "Invalid 'first'")
					.check(r -> r.getSecond() != null, "Invalid 'second'")
					.get();

			Player first = player.getGame().getPlayers().get(request.getFirst());
			Player second = player.getGame().getPlayers().get(request.getSecond());

			if (first == null || !first.isAlive() || second == null || !second.isAlive()) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			first.setLover(true);
			second.setLover(true);

			first.playSound(Sound.LOVE);
			second.playSound(Sound.LOVE);

			if (!player.equals(first) && !player.equals(second)) player.playSound(Sound.LOVE);
		}
	},
	SEER(Team.VILLAGE, false, false, false, false, 50) {
		@AllArgsConstructor
		public static class Response {
			private final Role role;
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.playSound(Sound.SEER, 1);
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
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				if (!player.getGame().<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				ctx.json(new Response(target.getRole()));
			});
		}
	},
	AURA_SEER(Team.VILLAGE, false, false, false, false, 5) {
		@AllArgsConstructor
		public static class Response {
			private final Team team;
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.playSound(Sound.SEER, 1);
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
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				if (!player.getGame().<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				ctx.json(new Response(target.getRole().displayTeam()));
			});
		}
	},
	WEREWOLF(Team.HOSTILE, false, true, true, false, 0) {
		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.playSound(Sound.HOWL, 0.5);
			game.evaluateVote().ifPresent(player -> game.setVictim(player.getId()));
		}

		@Override
		public boolean canSeeVictim(@NotNull Game game) {
			return true;
		}
	},
	WITCH(Team.VILLAGE, false, true, false, false, 100) {
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
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			Set<WitchAction> available = player.getGame().getRoleMetaData(this);
			Map<WitchAction, String> targets = ctx.bodyValidator(WitchRequest.class)
					.check(r -> r.getActions() != null, "Invalid 'action'")
					.get().getActions();

			Set<Runnable> execute = new HashSet<>();

			targets.forEach((action, target) -> {
				if (target == null || target.isBlank()) return;
				if (!available.contains(action)) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

				switch (action) {
					case HEAL -> {
						if (!target.equals(player.getGame().getVictim())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
						execute.add(() -> player.getGame().setVictim(null));
					}
					case POISON -> {
						Player p = Optional.ofNullable(player.getGame().getPlayers().get(target)).filter(Player::isAlive).orElseThrow(() -> new ErrorResponse(ErrorResponseType.INVALID_TARGET));
						execute.add(() -> p.kill(KillReason.WITCH_POISON));
					}
				}

				available.remove(action);
			});

			execute.forEach(Runnable::run);
			if (!execute.isEmpty()) player.getGame().playSound(Sound.POTION);
		}
	},
	VILLAGER_ELECT(Team.VILLAGE, true, true, false, false, 0) {
		@Override
		public boolean hasRole(@NotNull Player player) {
			return true;
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			game.playSound(Sound.VILLAGER);

			Optional.ofNullable(game.getVictim()).map(game.getPlayers()::get).ifPresent(p -> p.kill(KillReason.WEREWOLF_ATTACK));
			game.setVictim(null);
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return game.getPlayers().values().stream().filter(Player::isAlive).noneMatch(Player::isMayor);
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.evaluateVote().ifPresent(player -> player.setMayor(true));
		}
	},
	VILLAGER(Team.VILLAGE, true, true, false, false, 0) {
		@Override
		public void onTurnStart(@NotNull Game game) {
			game.playSound(Sound.VILLAGER);

			Optional.ofNullable(game.getVictim()).map(game.getPlayers()::get).ifPresent(p -> p.kill(KillReason.WEREWOLF_ATTACK));
			game.setVictim(null);
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.evaluateVote().ifPresent(player -> player.kill(KillReason.VILLAGE_VOTE));
		}

		@Override
		public boolean hasRole(@NotNull Player player) {
			return true;
		}
	},
	HUNTER(Team.VILLAGE, true, false, false, true, 10) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				target.kill(KillReason.HUNTER);
				player.getGame().playSound(Sound.SHOOT);
			});
		}
	},
	JESTER(Team.NEUTRAL, false, false, false, false, 2) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}
	},
	SPY(Team.VILLAGE, false, false, false, false, 1) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@NotNull
		@Override
		public Team displayTeam() {
			return Team.HOSTILE;
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final Team team;
	private final boolean day;
	private final boolean vote;
	private final boolean killing;
	private final boolean dead;
	private final int priority;

	public void onTurnStart(@NotNull Game game) {
		game.getPlayers().values().stream()
				.filter(this::hasRole)
				.forEach(p -> p.playSound(Sound.ACTIVE_TURN));
	}

	public void onTurnEnd(@NotNull Game game) {

	}

	public void handle(@NotNull Player player, @NotNull Context ctx) {
		player.getGame().getInteractions().put(player.getId(), getTarget(player.getGame(), ctx, Player::isAlive).map(Player::getId).orElse(""));
	}

	public void initialize(@NotNull Game game) { }

	@NotNull
	public Team displayTeam() {
		return team;
	}

	public boolean hasRole(@NotNull Player player) {
		return player.getRole() == this;
	}

	public boolean canUseRole(@NotNull Game game) {
		return true;
	}

	public boolean canSeeVictim(@NotNull Game game) {
		return false;
	}

	@NotNull
	private static Optional<Player> getTarget(@NotNull Game game, @NotNull Context ctx, @Nullable Predicate<Player> condition) {
		String target = ctx.bodyValidator(TargetRequest.class).get().getTarget();
		if (target == null || target.isBlank()) return Optional.empty();

		Player player = game.getPlayers().get(target);

		if (player == null) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
		if (condition != null && !condition.test(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

		return Optional.of(player);
	}

	@Getter
	private static class TargetRequest {
		private String target;
	}
}