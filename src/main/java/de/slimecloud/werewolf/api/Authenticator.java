package de.slimecloud.werewolf.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.slimecloud.werewolf.data.Game;
import de.slimecloud.werewolf.data.Player;
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
				.withClaimPresence("user")
				.withClaimPresence("game")
				.build();
	}

	@NotNull
	public String generateToken(@NotNull String user, @NotNull String game) {
		return JWT.create()
				.withClaim("user", user)
				.withClaim("game", game)
				.sign(algorithm);
	}

	@Contract("_, true -> !null; _, false -> _")
	public AuthorizationInfo checkAuthorization(@NotNull Context context, boolean required) {
		return checkAuthorization(context.header("Authorization") != null ? context.header("Authorization") : context.formParam("Authorization"), required);
	}

	@Contract("_, true -> !null; _, false -> _")
	public AuthorizationInfo checkAuthorization(@Nullable String authorization, boolean required) {
		if (authorization == null) {
			if (required) throw new ErrorResponse(ErrorResponseType.MISSING_TOKEN);
			else return null;
		}

		DecodedJWT decoded = parse(authorization);

		Game game = main.getGames().get(decoded.getClaim("game").asString());
		if(game == null) throw new ErrorResponse(ErrorResponseType.GAME_NOT_FOUND);

		Player player = game.getPlayers().get(decoded.getClaim("user").asString());
		if(player == null) throw new ErrorResponse(ErrorResponseType.PLAYER_LEFT);

		return new AuthorizationInfo(player, game);
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
