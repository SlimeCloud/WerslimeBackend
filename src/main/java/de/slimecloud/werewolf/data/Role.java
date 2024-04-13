package de.slimecloud.werewolf.data;

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
			super.handle(game, player, ctx);
		}
	},
	HUNTER(true, 1) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			super.handle(game, player, ctx);
		}
	},
	SEER(true, 2) {
		@Override
		public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {
			super.handle(game, player, ctx);
		}
	};

	private final boolean special;
	private final int priority;

	public void handle(@NotNull Game game, @NotNull Player player, @NotNull Context ctx) {

	}
}