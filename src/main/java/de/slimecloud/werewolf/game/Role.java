package de.slimecloud.werewolf.game;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.data.KillReason;
import de.slimecloud.werewolf.data.ProtocolEntry;
import de.slimecloud.werewolf.data.Sound;
import de.slimecloud.werewolf.main.Main;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Order is the order of awakening
 */
@Getter
@AllArgsConstructor
public enum Role implements IPlayerModifier {
	AMOR(List.of(Team.VILLAGE, Team.LOVER)) {
		@Getter
		public static class AmorRequest {
			private List<String> targets;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return super.canUseRole(game) && game.getPlayers().noneMatch(p -> p.hasTeam(Team.LOVER) && p.getRole() != AMOR);
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			AmorRequest request = ctx.bodyValidator(AmorRequest.class)
					.check(r -> r.getTargets() != null, "Invalid 'targets'")
					.check(r -> r.getTargets().size() == 2, "Invalid 'targets'")
					.get();

			Optional<Player> first = player.getGame().getPlayer(request.getTargets().get(0)).filter(Player::isAlive);
			Optional<Player> second = player.getGame().getPlayer(request.getTargets().get(1)).filter(Player::isAlive);

			if (first.isEmpty() || second.isEmpty() || first.equals(second)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			first.get().addTeam(Team.LOVER);
			second.get().addTeam(Team.LOVER);

			first.get().getModifiers().add(Modifier.LOVER);
			second.get().getModifiers().add(Modifier.LOVER);

			player.getGame().getPlayers()
					.filter(p -> p.hasTeam(Team.LOVER))
					.forEach(p -> p.playSound(Sound.LOVE));

			player.getGame().pushProtocol(ProtocolEntry.ProtocolType.AMOR, new String[] {
					first.get().getId(),
					second.get().getId()
			});
		}
	},
	SEER(Team.VILLAGE) {
		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>());
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.SEER, 1);
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				if (!player.getGame().<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				player.getGame().pushProtocol(ProtocolEntry.ProtocolType.SEER, new String[] { target.getId() });
			});
		}

		@Override
		public boolean canSeeRole(@NotNull Player player, @NotNull Player target) {
			return player.getGame().<Set<String>>getRoleMetaData(this).contains(target.getId());
		}
	},
	AURA_SEER(Team.VILLAGE) {
		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>());
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.SEER, 1);
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				if (!player.getGame().<Set<String>>getRoleMetaData(this).add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				player.getGame().pushProtocol(ProtocolEntry.ProtocolType.AURA_SEER, new String[] { target.getId() });
			});
		}

		@Override
		public boolean canSeeAura(@NotNull Player player, @NotNull Player target) {
			return player.getGame().<Set<String>>getRoleMetaData(this).contains(target.getId());
		}
	},
	WARLOCK(Team.WEREWOLF) {
		@Getter
		@Setter
		@RequiredArgsConstructor
		public static class WarlockMetaData {
			private int targetLimit = 2;

			private final Set<String> visible = new HashSet<>();
			private final Role camouflage;
		}

		public enum WarlockAction {
			VIEW, MARK
		}

		@Getter
		public static class Request {
			private Map<WarlockAction, String> actions;
		}

		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new WarlockMetaData(game.getPlayers()
					.map(Player::getRole)
					.filter(r -> r.getTeams().contains(Team.VILLAGE))
					.sorted((o1, o2) -> Main.random.nextInt(-1, 2))
					.findAny()
					.orElse(VILLAGER)
			));
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.WARLOCK, 1);
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			Map<WarlockAction, String> targets = ctx.bodyValidator(Request.class)
					.check(r -> r.getActions() != null, "Invalid 'actions'")
					.get().getActions();

			if (targets.containsKey(WarlockAction.MARK)) {
				if (player.getGame().<WarlockMetaData>getRoleMetaData(this).getTargetLimit() <= 0) throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST);

				Player target = player.getGame().getPlayer(targets.get(WarlockAction.MARK)).filter(Player::isAlive).orElseThrow(() -> new ErrorResponse(ErrorResponseType.INVALID_TARGET));
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				player.getGame().getRoleMetaData().put(WEREWOLF, target.id);
				player.getGame().<WarlockMetaData>getRoleMetaData(this).targetLimit--;

				player.getGame().pushProtocol(ProtocolEntry.ProtocolType.WARLOCK_MARK, new String[] { target.getId() });
			}

			if (targets.containsKey(WarlockAction.VIEW)) {
				Player target = player.getGame().getPlayer(targets.get(WarlockAction.VIEW)).filter(Player::isAlive).orElseThrow(() -> new ErrorResponse(ErrorResponseType.INVALID_TARGET));

				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				if (!player.getGame().<WarlockMetaData>getRoleMetaData(this).getVisible().add(target.getId())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

				player.getGame().pushProtocol(ProtocolEntry.ProtocolType.WARLOCK_VIEW, new String[] { target.getId() });
			}
		}

		@Override
		public boolean canSeeRole(@NotNull Player player, @NotNull Player target) {
			return player.getGame().<WarlockMetaData>getRoleMetaData(this).getVisible().contains(target.getId());
		}

		@NotNull
		@Override
		public Role getEffectiveRole(@NotNull Game game) {
			return game.<WarlockMetaData>getRoleMetaData(this).getCamouflage();
		}
	},
	WEREWOLF(Team.WEREWOLF, EnumSet.of(RoleFlag.VOTE, RoleFlag.VICTIM, RoleFlag.KILLING)) {
		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.HOWL, 0.5);
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.evaluateVote().ifPresent(player -> {
				game.setVictim(player.getId());
				game.pushProtocol(ProtocolEntry.ProtocolType.WEREWOLF, new String[] { player.getId() });
			});
		}

		@Override
		public boolean handleDeath(@NotNull Player player, @NotNull KillReason reason) {
			if (player.getGame().getPlayers().filter(Player::isAlive).noneMatch(p -> p.getRole() == Role.WEREWOLF)) {
				player.getGame().getPlayers().filter(Player::isAlive).filter(p -> p.getRole() == Role.WARLOCK).forEach(p -> p.setRole(Role.WEREWOLF));
			}

			return super.handleDeath(player, reason);
		}

		@Override
		public boolean canSeeAura(@NotNull Player player, @NotNull Player target) {
			return player.getTeams().contains(Team.WEREWOLF) && (target.getRole() == WEREWOLF || target.getRole() == SPY);
		}

		@Override
		public boolean canSeeInteractions(@NotNull Player player) {
			return false;
		}

		@Override
		public boolean canSeeTarget(@NotNull Player player) {
			return player.getTeams().contains(Team.WEREWOLF) || player.getRole() == SPY;
		}
	},
	WITCH(Team.VILLAGE, EnumSet.of(RoleFlag.VICTIM)) {
		public enum WitchAction {
			POISON,
			HEAL
		}

		@Getter
		public static class Request {
			private Map<WitchAction, String> actions;
		}

		@Override
		public void initialize(@NotNull Game game) {
			game.getRoleMetaData().put(this, new HashSet<>(Arrays.asList(WitchAction.POISON, WitchAction.HEAL)));
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.POTION);
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			Set<WitchAction> available = player.getGame().getRoleMetaData(this);
			Map<WitchAction, String> targets = ctx.bodyValidator(Request.class)
					.check(r -> r.getActions() != null, "Invalid 'actions'")
					.get().getActions();

			Set<Runnable> execute = new HashSet<>();

			targets.forEach((action, target) -> {
				if (target == null || target.isBlank()) return;
				if (!available.contains(action)) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

				switch (action) {
					case HEAL -> {
						if (!target.equals(player.getGame().getVictim())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
						execute.add(() -> {
							player.getGame().setVictim(null);
							player.getGame().pushProtocol(ProtocolEntry.ProtocolType.WITCH_HEAL, new String[] { target });
						});
					}
					case POISON -> {
						Player p = player.getGame().getPlayer(target).filter(Player::isAlive).orElseThrow(() -> new ErrorResponse(ErrorResponseType.INVALID_TARGET));
						execute.add(() -> {
							p.kill(KillReason.WITCH_POISON);
							player.getGame().pushProtocol(ProtocolEntry.ProtocolType.WITCH_POISON, new String[] { target });
						});
					}
				}

				available.remove(action);
			});

			execute.forEach(Runnable::run);
		}
	},
	MORNING(Collections.emptyList(), EnumSet.of(RoleFlag.DAY)) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return true;
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			game.next();
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.getNightActions().forEach(Runnable::run);
			game.getNightActions().clear();

			Optional.ofNullable(game.getVictim()).flatMap(game::getPlayer).ifPresent(p -> p.kill(KillReason.WEREWOLF_ATTACK));
			game.setVictim(null);
		}
	},
	VILLAGER_ELECT(Collections.emptyList(), EnumSet.of(RoleFlag.VOTE, RoleFlag.DAY)) {
		@Override
		public boolean hasRole(@NotNull Player player) {
			return true;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return game.getPlayers().filter(Player::isAlive).noneMatch(p -> p.hasModifier(Modifier.MAYOR));
		}

		@Override
		public void onTurnStart(@NotNull Game game) {
			super.onTurnStart(game);
			game.playSound(Sound.VILLAGER);
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.evaluateVote().ifPresent(player -> {
				player.getModifiers().add(Modifier.MAYOR);
				game.pushProtocol(ProtocolEntry.ProtocolType.VILLAGER_ELECT, new String[] { player.getId() });
			});
		}
	},
	VILLAGER(Team.VILLAGE, EnumSet.of(RoleFlag.VOTE, RoleFlag.DAY)) {
		@Override
		public boolean hasRole(@NotNull Player player) {
			return true;
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.evaluateVote().ifPresent(player -> {
				player.kill(KillReason.VILLAGE_VOTE);
				game.pushProtocol(ProtocolEntry.ProtocolType.VILLAGER, new String[] { player.getId() });
			});
		}
	},
	HUNTER(Team.VILLAGE, EnumSet.of(RoleFlag.DAY)) {
		@Getter
		@RequiredArgsConstructor
		public static class HunterMetaData {
			private final Role lastRole;
			private final KillReason reason;
		}

		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@Override
		public void handle(@NotNull Player player, @NotNull Context ctx) {
			getTarget(player.getGame(), ctx, Player::isAlive).ifPresent(target -> {
				if (target.equals(player)) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
				target.kill(KillReason.HUNTER);
				player.getGame().pushProtocol(ProtocolEntry.ProtocolType.HUNTER, new String[] { target.getId() });
			});

			player.kill(player.getGame().<HunterMetaData>getRoleMetaData(HUNTER).getReason());
			player.getGame().playSound(Sound.SHOOT);
			player.getGame().next();
		}

		@Override
		public void onTurnEnd(@NotNull Game game) {
			game.setCurrent(game.<HunterMetaData>getRoleMetaData(HUNTER).getLastRole());
		}

		@Override
		public boolean handleDeath(@NotNull Player player, @NotNull KillReason reason) {
			player.getGame().getRoleMetaData().put(Role.HUNTER, new HunterMetaData(player.getGame().getCurrent(), reason));
			player.getGame().setCurrent(Role.HUNTER);

			return false;
		}
	},
	JESTER(List.of(Team.JESTER)) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@Override
		public boolean handleDeath(@NotNull Player player, @NotNull KillReason reason) {
			if (reason != KillReason.VILLAGE_VOTE) return super.handleDeath(player, reason);

			player.getGame().sendWin(Team.JESTER);
			return false;
		}
	},
	SPY(List.of(Team.VILLAGE), EnumSet.of(RoleFlag.VICTIM)) {
		@Override
		public boolean canUseRole(@NotNull Game game) {
			return false;
		}

		@NotNull
		@Override
		public Aura getEffectiveAura(@NotNull Game game) {
			return Aura.HOSTILE;
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final List<Team> teams;
	private final EnumSet<RoleFlag> flags;

	Role(@NotNull List<Team> teams) {
		this(teams, EnumSet.noneOf(RoleFlag.class));
	}

	Role(@NotNull Team team) {
		this(List.of(team));
	}

	Role(@NotNull Team team, @NotNull EnumSet<RoleFlag> flags) {
		this(List.of(team), flags);
	}

	public final boolean hasFlag(@NotNull RoleFlag flag) {
		return flags.contains(flag);
	}

	public void initialize(@NotNull Game game) { }

	public boolean hasRole(@NotNull Player player) {
		return player.getRole() == this;
	}

	public boolean canUseRole(@NotNull Game game) {
		return game.getPlayers().filter(Player::isAlive).anyMatch(this::hasRole);
	}

	public void onTurnStart(@NotNull Game game) {
		game.getPlayers()
				.filter(this::hasRole)
				.filter(Player::isAlive)
				.forEach(p -> p.playSound(Sound.ACTIVE_TURN));
	}

	public void handle(@NotNull Player player, @NotNull Context ctx) {
		player.getGame().getInteractions().put(player.getId(), getTarget(player.getGame(), ctx, Player::isAlive).map(Player::getId).orElse(""));
	}


	public void onTurnEnd(@NotNull Game game) {

	}

	public boolean canSeeInteractions(@NotNull Player player) {
		return hasRole(player) || player.isSpectating();
	}

	public boolean canSeeTarget(@NotNull Player player) {
		return canSeeInteractions(player);
	}

	@NotNull
	public Role getEffectiveRole(@NotNull Game game) {
		return this;
	}

	@Nullable
	public Aura getEffectiveAura(@NotNull Game game) {
		return Aura.of(teams).orElse(null);
	}


	@NotNull
	private static Optional<Player> getTarget(@NotNull Game game, @NotNull Context ctx, @Nullable Predicate<Player> condition) {
		String target = ctx.bodyValidator(TargetRequest.class).get().getTarget();
		if (target == null || target.isBlank()) return Optional.empty();

		return game.getPlayer(target)
				.filter(p -> condition == null || condition.test(p))
				.or(() -> { throw new ErrorResponse(ErrorResponseType.INVALID_TARGET); });
	}

	@Getter
	private static class TargetRequest {
		private String target;
	}
}