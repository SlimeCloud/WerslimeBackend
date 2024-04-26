package de.slimecloud.werewolf.config;

import de.slimecloud.werewolf.main.Main;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Getter
public class Config {
	private int port;

	private ActivityConfig activity;
	private List<LogForwarding> logForwarding = Collections.emptyList();

	@NotNull
	public static Config readFromFile(@NotNull String path) throws IOException {
		try (FileReader reader = new FileReader(path)) {
			return Main.json.fromJson(reader, Config.class);
		}
	}
}
