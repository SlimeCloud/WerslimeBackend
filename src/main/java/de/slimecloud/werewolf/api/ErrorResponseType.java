package de.slimecloud.werewolf.api;

import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorResponseType {
	UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST),

	MISSING_TOKEN(HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
	MISSING_ACCESS(HttpStatus.FORBIDDEN),

	GAME_NOT_FOUND(HttpStatus.NOT_FOUND),
	INVALID_GAME_STATE(HttpStatus.CONFLICT),

	INVALID_TARGET(HttpStatus.BAD_REQUEST),
	INVALID_TURN(HttpStatus.FORBIDDEN),
	ACTION_UNAVAILABLE(HttpStatus.CONFLICT);

	private final HttpStatus status;
}
