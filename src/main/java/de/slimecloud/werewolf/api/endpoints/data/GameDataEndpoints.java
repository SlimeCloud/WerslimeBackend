package de.slimecloud.werewolf.api.endpoints.data;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class GameDataEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		get("/{id}", new InfoEndpoint());
		get(new ListEndpoint());
		post(new CreateEndpoint());
	}
}
