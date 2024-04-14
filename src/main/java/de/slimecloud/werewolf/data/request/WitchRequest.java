package de.slimecloud.werewolf.data.request;

import lombok.Getter;

@Getter
public class WitchRequest extends TargetRequest {

	private Type type;

	public enum Type {
		HEAL,
		KILL
	}

}
