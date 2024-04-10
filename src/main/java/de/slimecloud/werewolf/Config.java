package de.slimecloud.werewolf;

import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;

public class Config {
	public int port;

	@NotNull
	public static Config readFromFile(@NotNull String file) throws IOException {
		try(FileReader reader = new FileReader(file)) {
			return Main.json.fromJson(reader, Config.class);
		}
	}
}
