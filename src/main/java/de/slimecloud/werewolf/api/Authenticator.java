package de.slimecloud.werewolf.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.slimecloud.werewolf.main.Main;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;

public class Authenticator {
	private final Main main;

	private final Algorithm algorithm;

	private final JWTVerifier verifier;

	public Authenticator(@NotNull Main main) {
		this.main = main;

		algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(main.getCredentials().get("AUTH_SECRET")));

		verifier = JWT.require(algorithm)
				.withClaimPresence("sub")
				.build();
	}

	@NotNull
	public String generateToken(@NotNull String id) {
		return JWT.create()
				.withSubject(id)
				.sign(algorithm);
	}

	@Contract("_, true -> !null; _, false -> _")
	public String checkAuthorization(@NotNull Context context, boolean required) {
		return checkAuthorization(context.header("Authorization") != null ? context.header("Authorization") : context.formParam("Authorization"), required);
	}

	@Contract("_, true -> !null; _, false -> _")
	public String checkAuthorization(@Nullable String authorization, boolean required) {
		if (authorization == null) {
			if (required) throw new ErrorResponse(ErrorResponseType.MISSING_TOKEN);
			else return null;
		}

		DecodedJWT decoded = parse(authorization);

		return decoded.getSubject();
	}

	@NotNull
	private DecodedJWT parse(@Nullable String token) {
		if (token == null || token.isBlank()) throw new ErrorResponse(ErrorResponseType.MISSING_TOKEN);

		try {
			return verifier.verify(token);
		} catch (TokenExpiredException e) {
			throw new ErrorResponse(ErrorResponseType.TOKEN_EXPIRED);
		} catch (SignatureVerificationException | InvalidClaimException | JWTDecodeException e) {
			throw new ErrorResponse(ErrorResponseType.INVALID_TOKEN);
		} catch (Exception e) {
			throw new InternalServerErrorResponse(e.getMessage());
		}
	}
}