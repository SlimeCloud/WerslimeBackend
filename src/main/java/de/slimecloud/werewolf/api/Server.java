package de.slimecloud.werewolf.api;

import com.google.gson.JsonSyntaxException;
import de.slimecloud.werewolf.api.endpoints.CreateEndpoint;
import de.slimecloud.werewolf.api.endpoints.JoinEndpoint;
import de.slimecloud.werewolf.api.endpoints.MeEndpoint;
import de.slimecloud.werewolf.api.endpoints.game.GameInfoEndpoint;
import de.slimecloud.werewolf.main.Main;
import io.javalin.Javalin;
import io.javalin.config.Key;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

@Slf4j
public class Server {
	public final static Key<Main> MAIN_KEY = new Key<>("main");

	private final Main main;
	private final Javalin server;

	public Server(Main main) {
		this.main = main;
		this.server = Javalin.create(config -> {
			config.showJavalinBanner = false;

			config.http.defaultContentType = "text/json";
			config.useVirtualThreads = true;

			config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
			config.jsonMapper(new JsonMapper() {
				@NotNull
				@Override
				public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
					return Main.json.fromJson(json, targetType);
				}

				@NotNull
				@Override
				public String toJsonString(@NotNull Object obj, @NotNull Type type) {
					return Main.json.toJson(obj, type);
				}
			});

			config.router.apiBuilder(() -> {
				get("/@me", new MeEndpoint());

				post("/games", new CreateEndpoint());

				get("/games/{id}", new GameInfoEndpoint());
				post("/games/{id}/join", new JoinEndpoint());
			});

			config.appData(MAIN_KEY, main);
		});

		server.exception(JsonSyntaxException.class, (e, ctx) -> {throw new ErrorResponse(ErrorResponseType.INVALID_SYNTAX, Map.of("message", e.getMessage()));});
		server.exception(ValidationException.class, (e, ctx) -> {
			throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST, Map.of(
					"fields",
					e.getErrors().values().stream().findFirst().map(v -> v.stream().map(ValidationError::getMessage).toList()).orElse(Collections.emptyList())
			));
		});

		server.exception(Exception.class, (e, ctx) -> {
			logger.error("Error in http handler", e);
			throw new InternalServerErrorResponse();
		});

		server.exception(HttpResponseException.class, (e, ctx) -> {
			throw new ErrorResponse(
					HttpStatus.forStatus(e.getStatus()),
					Map.of("message", e.getMessage())
			);
		});

		server.exception(ErrorResponse.class, (e, ctx) -> {
			try {
				ctx.status(e.status).json(e.toData());
			} catch (Exception ex) {
				logger.error("Error sending response", ex);
				ctx.status(500).json(new ErrorResponse(ErrorResponseType.UNKNOWN).toData());
			}
		});
	}

	public void start() {
		server.start(main.getConfig().port);
	}

	public void stop() {
		server.stop();
	}
}
