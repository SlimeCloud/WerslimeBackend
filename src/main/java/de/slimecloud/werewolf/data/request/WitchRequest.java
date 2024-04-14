package de.slimecloud.werewolf.data.request;

import lombok.Getter;

@Getter
public class WitchRequest extends TargetRequest {

	private WitchAction action;

	public enum WitchAction {
		HEAL,
		KILL
	}

}
