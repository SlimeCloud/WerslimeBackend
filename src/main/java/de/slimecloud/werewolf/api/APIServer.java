package de.slimecloud.werewolf.api;

import de.slimecloud.werewolf.Main;
import de.slimecloud.werewolf.WerewolfServer;
import de.slimecloud.werewolf.api.endpoints.AuthenticationEndpoint;
import io.javalin.Javalin;
import io.javalin.config.Key;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import static io.javalin.apibuilder.ApiBuilder.post;

@Slf4j
public class APIServer {
	public final static Key<WerewolfServer> MAIN_KEY = new Key<>("main");

	private final Javalin server;

	public APIServer(@NotNull WerewolfServer main) {
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
				post("/token", new AuthenticationEndpoint());
			});

			config.appData(MAIN_KEY, main);
		});

		this.server.exception(Exception.class, (e, ctx) -> {
			logger.error("Error in http handler", e);
			throw new InternalServerErrorResponse();
		});

		this.server.start(main.getConfig().port);
	}
}
