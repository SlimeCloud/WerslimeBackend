package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.api.endpoints.CreateEndpoint;
import de.slimecloud.werewolf.api.endpoints.JoinEndpoint;
import de.slimecloud.werewolf.data.Role;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class GameEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		post(new CreateEndpoint());

		post("/join", new JoinEndpoint());
		post("/settings", new SettingsEndpoint());
		post("/start", new StartEndpoint());

		path("/action", () -> {
			for (Role role : Role.values()) {
				if (!role.isSpecial()) continue;
				post("/" + role.name().toLowerCase(), new RoleActionEndpoint(role));
			}
		});
	}

	@AllArgsConstructor
	public static class RoleActionEndpoint implements Handler {
		private final Role role;

		@Override
		public void handle(@NotNull Context ctx) throws Exception {
			AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);

			if (info.getPlayer().getRole() != role) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);
			if (info.getGame().getCurrent() != role) throw new ErrorResponse(ErrorResponseType.INVALID_TURN);

			role.handle(info.getGame(), info.getPlayer(), ctx);
		}
	}
}