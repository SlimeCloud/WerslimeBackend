package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.data.request.TargetRequest;
import de.slimecloud.werewolf.data.request.WitchRequest;
import io.javalin.http.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public enum Role {
	VILLAGER(false, 0),
	WEREWOLF(false, 0),
	WITCH(true, 3) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			WitchRequest request = ctx.bodyValidator(WitchRequest.class).get();
			//TODO implement role logic
		}
	},
	HUNTER(true, 1) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			TargetRequest request = ctx.bodyValidator(TargetRequest.class).get();
			//TODO implement role logic
		}
	},
	SEER(true, 2) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			TargetRequest request = ctx.bodyValidator(TargetRequest.class).get();
			//TODO implement role logic
		}
	};

	private final boolean special;
	private final int priority;


	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {

	}
}