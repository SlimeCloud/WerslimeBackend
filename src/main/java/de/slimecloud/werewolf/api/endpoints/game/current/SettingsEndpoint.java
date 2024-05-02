package de.slimecloud.werewolf.api.endpoints.game.current;

import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.GameSettings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SettingsEndpoint implements Handler {
	@Override
	public void handle(@NotNull Context ctx) throws Exception { //TODO allow to modify fields separately
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		GameSettings settings = ctx.bodyValidator(GameSettings.class)
				.check(s -> s.getRoles() != null && s.getRoles().stream().noneMatch(Objects::isNull), "Invalid 'roles'")
				.check(s -> s.getWerewolfAmount() >= 1, "Invalid 'werewolfAmount'")
				.get();

		info.getGame().setSettings(settings);

		info.getGame().sendUpdate();
	}
}
