package de.slimecloud.werewolf.api.endpoints.game;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;

public class GameEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		post("join", new JoinEndpoint());
		post("leave", new LeaveEndpoint());
		post("kick", new KickEndpoint());
		post("promote", new PromoteEndpoint());

		post("settings", new SettingsEndpoint());
		post("reset", new ResetEndpoint());
		post("start", new StartEndpoint());

		post("next", new NextEndpoint());
		post("action", new RoleActionEndpoint());
	}
}
