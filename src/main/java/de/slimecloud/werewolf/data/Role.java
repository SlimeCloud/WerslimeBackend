package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.data.request.TargetRequest;
import de.slimecloud.werewolf.data.request.WitchRequest;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Role {
	SEER(true, true, 2) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			if (!player.isAlive()) return;
			TargetRequest request = ctx.bodyValidator(TargetRequest.class).get();

			Player target = game.getPlayers().get(request.getId());
			checkAlive(target, true);

			game.getSeerVisible().add(target.getId().toString());

			ctx.json(target.getRole());
		}
	},
	WEREWOLF(false, true, 0),
	WITCH(true, true, 3) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			if (!player.isAlive()) return;
			WitchRequest request = ctx.bodyValidator(WitchRequest.class)
					.check(r -> r.getAction() != null, "Invalid 'action'")
					.check(r -> r.getAction() == WitchRequest.WitchAction.HEAL || validateId(r, game), "Invalid 'id'")
					.get();

			if (!game.getWitchActions().contains(request.getAction())) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);

			Player target = game.getPlayers().get(request.getId());
			switch (request.getAction()) {
				case HEAL -> game.setVictim(null);
				case KILL -> {
					checkAlive(target, true);
					target.kill(game);
				}
			}

			game.getWitchActions().remove(request.getAction());
		}
	},
	VILLAGER(false, true, 0),
	HUNTER(true, false, 1) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			if (!player.isAlive()) return;
			TargetRequest request = ctx.bodyValidator(TargetRequest.class)
					.check(r -> validateId(r, game), "Invalid 'id'")
					.get();

			Player target = game.getPlayers().get(request.getId());
			checkAlive(target, true);
			target.kill(game);
		}
	};

	public final static List<Role> values = Arrays.asList(values());

	private final boolean special;
	private final boolean automatic;
	private final int priority;


	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
		if (!player.isAlive()) return;
		TargetRequest request = ctx.bodyValidator(TargetRequest.class)
				.check(r -> validateId(r, game), "Invalid 'id'")
				.get();

		Player target = game.getPlayers().get(request.getId());
		checkAlive(target, true);

		game.getVotes().put(player.getId().toString(), target.getId().toString());
	}

	private static boolean validateId(@NotNull TargetRequest request, @NotNull Game game) {
		return request.getId() != null && game.getPlayers().containsKey(request.getId());
	}

	private static void checkAlive(@NotNull Player player, boolean shouldAlive) {
		if (player.isAlive() != shouldAlive) throw new ErrorResponse(ErrorResponseType.INVALID_TARGET);
	}
}