package de.slimecloud.werewolf.api;

import io.javalin.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorResponseType {
	UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR),

	INVALID_SYNTAX(HttpStatus.BAD_REQUEST), //message: string
	INVALID_REQUEST(HttpStatus.BAD_REQUEST), //fields: []

	MISSING_TOKEN(HttpStatus.UNAUTHORIZED),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
	TOKEN_INVALID_HASH(HttpStatus.FORBIDDEN),
	MISSING_ACCESS(HttpStatus.FORBIDDEN),

	GAME_STARTED(HttpStatus.CONFLICT),
	GAME_NOT_FOUND(HttpStatus.NOT_FOUND);

	private final HttpStatus status;
}
