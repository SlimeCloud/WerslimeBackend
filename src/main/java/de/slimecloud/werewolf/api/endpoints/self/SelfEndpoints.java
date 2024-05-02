package de.slimecloud.werewolf.api.endpoints.self;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class SelfEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		get(new InfoEndpoint());
		patch(new RenameEndpoint());

		path("game", () -> {
			put("{game_id}", new JoinEndpoint());
			delete(new LeaveEndpoint());
		});
	}
}
