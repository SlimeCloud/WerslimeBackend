package de.slimecloud.werewolf.api;

import com.google.gson.JsonSyntaxException;
import de.slimecloud.werewolf.api.endpoints.EventSource;
import de.slimecloud.werewolf.api.endpoints.MeEndpoint;
import de.slimecloud.werewolf.api.endpoints.data.GameDataEndpoints;
import de.slimecloud.werewolf.api.endpoints.game.GameEndpoints;
import de.slimecloud.werewolf.main.Main;
import io.javalin.Javalin;
import io.javalin.config.Key;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.validation.ValidationException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import static io.javalin.apibuilder.ApiBuilder.*;

@Slf4j
@Getter
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
				sse("/events", new EventSource());

				path("/games", new GameDataEndpoints());
				path("/game", new GameEndpoints());
			});

			config.appData(MAIN_KEY, main);
		});

		server.exception(JsonSyntaxException.class, (e, ctx) -> { throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST); });
		server.exception(ValidationException.class, (e, ctx) -> { throw new ErrorResponse(ErrorResponseType.INVALID_REQUEST); });

		server.exception(Exception.class, (e, ctx) -> {
			logger.error("Error in http handler", e);
			throw new InternalServerErrorResponse();
		});

		server.exception(HttpResponseException.class, (e, ctx) -> { throw new ErrorResponse(HttpStatus.forStatus(e.getStatus())); });

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
