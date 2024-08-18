import de.slimecloud.werewolf.game.Game;
import de.slimecloud.werewolf.game.Player;
import de.slimecloud.werewolf.game.Role;
import de.slimecloud.werewolf.main.Main;
import de.slimecloud.werewolf.main.config.Config;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.Set;

public class WarlockTest {
	public static void main(String[] args) throws Exception {
		Main main = new Main(Config.readFromFile("config"), Dotenv.configure().filename("credentials").load());
		Game game = main.create("a");

		Player a = game.getPlayers().findFirst().orElseThrow();
		Player b = game.join("b");
		Player c = game.join("c");

		game.getSettings().roles(List.of(Role.SEER, Role.WARLOCK));
		game.getSettings().werewolfAmount(1);
		game.getSettings().isPublic(true);

		game.start();

		a.setRole(Role.SEER);
		b.setRole(Role.WARLOCK);
		c.setRole(Role.WEREWOLF);

		System.out.println(b.getAura(c));

		System.out.println(b.getAura(a));
		game.<Set<String>>getRoleMetaData(Role.SEER).add(b.getId());
		System.out.println(b.getAura(a));
	}
}
