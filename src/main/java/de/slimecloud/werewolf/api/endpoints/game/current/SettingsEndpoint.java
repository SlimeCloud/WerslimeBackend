package de.slimecloud.werewolf.api.endpoints.game.current;

import de.cyklon.reflection.entities.ReflectClass;
import de.cyklon.reflection.entities.members.ReflectField;
import de.cyklon.reflection.types.Modifier;
import de.slimecloud.werewolf.api.AuthorizationInfo;
import de.slimecloud.werewolf.api.ErrorResponse;
import de.slimecloud.werewolf.api.ErrorResponseType;
import de.slimecloud.werewolf.api.Server;
import de.slimecloud.werewolf.data.GameSettings;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class SettingsEndpoint implements Handler {
	public final static ReflectClass<GameSettings> CLAZZ = ReflectClass.wrap(GameSettings.class);

	@Override
	public void handle(@NotNull Context ctx) throws Exception {
		AuthorizationInfo info = ctx.appData(Server.MAIN_KEY).getAuthenticator().checkAuthorization(ctx, true);
		if (!info.getPlayer().isMaster()) throw new ErrorResponse(ErrorResponseType.MISSING_ACCESS);

		GameSettings settings = ctx.bodyValidator(GameSettings.class).get();

		for (ReflectField<?, ?> field : CLAZZ.getFields(f -> !f.hasModifier(Modifier.STATIC))) {
			Object value = field.getValue(settings);
			if (value == null) continue;
			if (!GameSettings.validate(field.getName(), value)) throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST);

			field.setValue(info.getGame().getSettings(), value);
		}

		info.getGame().sendUpdate();
	}
}
