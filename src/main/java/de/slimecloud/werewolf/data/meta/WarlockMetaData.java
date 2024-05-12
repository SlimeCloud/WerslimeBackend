package de.slimecloud.werewolf.data.meta;

import de.slimecloud.werewolf.data.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class WarlockMetaData {
	private int targetLimit = 2;
	private String target = null;

	private final Set<String> visible = new HashSet<>();
	private final Role camouflage;
}