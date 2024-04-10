package de.slimecloud.werewolf.api.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class AuthenticationEndpoint implements Handler {
	@Getter
	public static class Request {
		public String code;
	}

	@AllArgsConstructor
	public static class Response {
		public String discord_token;
		public String api_token;
	}

	@Override
	public void handle(@NotNull Context context) throws Exception {
		var code = context.bodyAsClass(Request.class).code;

		context.json(new Response("", ""));
	}
}
