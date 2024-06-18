package de.slimecloud.werewolf.data;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProtocolEntry {
	private final String id;
	private final ProtocolType type;
	private final Object[] data;

	public enum ProtocolType {
		START,
		AMOR,
		SEER,
		AURA_SEER,
		WARLOCK_VIEW,
		WARLOCK_MARK,
		HEALER,
		WEREWOLF,
		WEREWOLF_HEALER,
		WITCH_HEAL,
		WITCH_POISON,
		HUNTER,
		VILLAGER,
		VILLAGER_ELECT,
		DEATH,
		END
	}
}
