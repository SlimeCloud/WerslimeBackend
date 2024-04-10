package de.slimecloud.werewolf.main;

import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
	public final static Gson json = new Gson();

	public static void main(String[] args) throws Exception {
		new WerewolfServer(
				Config.readFromFile("config.json"),
				Dotenv.configure().filename("credentials").load()
		);
	}
}