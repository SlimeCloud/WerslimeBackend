package de.slimecloud.werewolf.api;

import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

public class ErrorResponse extends RuntimeException {
	public record Data(int status, ErrorResponseType type) {
	}

	public final int status;
	public final ErrorResponseType type;

	public ErrorResponse(@NotNull ErrorResponseType type) {
		this.status = type.getStatus().getCode();
		this.type = type;
	}

	public ErrorResponse(@NotNull HttpStatus e) {
		this.status = e.getCode();
		this.type = ErrorResponseType.UNKNOWN;
	}

	@NotNull
	public Data toData() {
		return new Data(status, type);
	}
}
