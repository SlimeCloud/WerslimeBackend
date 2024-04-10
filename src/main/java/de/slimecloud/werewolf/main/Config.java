package de.slimecloud.werewolf.main;

import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

public class Config {
	public int port;

	@NotNull
	public static Config readFromFile(@NotNull String path) throws IOException {
		try (FileReader reader = new FileReader(path)) {
			return Main.json.fromJson(reader, Config.class);
		}
	}
}
