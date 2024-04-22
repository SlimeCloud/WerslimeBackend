package de.slimecloud.werewolf.api.endpoints.data;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;

public class GameDataEndpoints implements EndpointGroup {
	@Override
	public void addEndpoints() {
		post(new CreateEndpoint());
		get(new ListEndpoint());
	}
}
