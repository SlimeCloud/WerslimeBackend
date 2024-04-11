package de.slimecloud.werewolf.data;

import de.slimecloud.werewolf.main.Main;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	VILLAGER("Villager"),
	WEREWOLF("Werewolf"),
	WITCH("Witch"),
	HUNTER("Hunter"),
	AMOR("Amor"),
	SEER("Seer");

	final String name;

	public static Role getRandom() {
		return values()[Main.random.nextInt(values().length - 1)];
	}
}