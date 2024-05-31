package de.slimecloud.werewolf.api.endpoints.game.current;

import de.mineking.javautils.ID;
import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.game.EventType;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

public class ChatEndpoint implements Handler {
	@Getter
	public static class Request {
		private String message;
	}

	@RequiredArgsConstructor
	public static class Message {
		private final String id = ID.generate().asString();
		private final String author;
		private final String message;
	}

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);

		if (!info.getGame().getSettings().chat()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);
		if (!info.getGame().getCurrent().hasRole(info.getPlayer())) throw new ErrorResponse(ErrorResponseType.INVALID_GAME_STATE);
		if (!info.getPlayer().isAlive()) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

		Request request = ctx.bodyValidator(Request.class)
				.check(r -> r.getMessage() != null && !r.getMessage().isBlank() && r.getMessage().length() <= 100, "Invalid 'message'")
				.get();

		Message message = new Message(info.getPlayer().getId(), request.getMessage());

		info.getGame().getAllPlayers().values().stream()
				.filter(p -> (p.isAlive() && info.getGame().getCurrent().canSeeChat(p)) || p.isSpectating())
				.forEach(p -> p.sendEvent(EventType.CHAT, message));
	}
}
