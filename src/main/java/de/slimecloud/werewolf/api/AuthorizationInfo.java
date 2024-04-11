package de.slimecloud.werewolf.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthorizationInfo {
	private final String user;
	private final String game;
}
