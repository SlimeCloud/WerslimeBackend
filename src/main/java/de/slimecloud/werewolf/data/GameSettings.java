package de.slimecloud.werewolf.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class GameSettings {
	int werewolfAmount = 2;
	List<String> roles = Arrays.asList("Villager", "Werewolf", "Witch", "General", "Hunter", "Amor", "seer");
}
