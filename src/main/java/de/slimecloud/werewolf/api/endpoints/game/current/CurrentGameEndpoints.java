package de.slimecloud.werewolf.api.endpoints.game.current;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CurrentGameEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		patch(new SettingsEndpoint());

		path("members/{user_id}", () -> {
			delete(new KickEndpoint());
			patch(new ModifyMemberEndpoint());
		});

		path("session", () -> {
			put(new StartEndpoint());
			delete(new ResetEndpoint());
			patch(new NextEndpoint());
		});

		post("action", new RoleActionEndpoint());
		post("chat", new ChatEndpoint());
	}
}
