package de.slimecloud.werewolf.api.endpoints.game;

import de.slimecloud.werewolf.api.endpoints.game.current.CurrentGameEndpoints;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GameEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		get(new ListEndpoint());
		post(new CreateEndpoint());

		path("@me", new CurrentGameEndpoints());
		get("{game_id}", new InfoEndpoint());
	}
}
