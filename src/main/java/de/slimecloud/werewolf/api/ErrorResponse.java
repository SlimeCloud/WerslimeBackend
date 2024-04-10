package de.slimecloud.werewolf.api;

import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class ErrorResponse extends RuntimeException {
	public record Data(int status, ErrorResponseType type, Map<String, Object> args) {
	}

	public final int status;
	public final ErrorResponseType type;
	public final Map<String, Object> args;

	public ErrorResponse(@NotNull ErrorResponseType type, @NotNull Map<String, Object> args) {
		this.status = type.getStatus().getCode();
		this.type = type;
		this.args = args;
	}

	public ErrorResponse(@NotNull ErrorResponseType type) {
		this(type, Collections.emptyMap());
	}

	public ErrorResponse(@NotNull HttpStatus e, @NotNull Map<String, Object> args) {
		this.status = e.getCode();
		this.type = ErrorResponseType.UNKNOWN;
		this.args = args;
	}

	@NotNull
	public Data toData() {
		return new Data(status, type, args);
	}
}
