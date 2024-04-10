package de.slimecloud.werewolf.api;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import de.slimecloud.werewolf.WerewolfServer;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

public class Authenticator {
	private final Algorithm algorithm;
	private final JWTVerifier verifier;

	public Authenticator(@NotNull WerewolfServer main) {
		this.algorithm = Algorithm.HMAC512(Base64.getDecoder().decode(main.getCredentials().get("AUTH_SECRET")));
		this.verifier = JWT.require(algorithm)
				.build();
	}

	@NotNull
	public String generateToken(@NotNull String id) {
		return JWT.create()
				.withSubject(id)
				.sign(algorithm);
	}

	@NotNull
	public String verifyToken(@NotNull String token) {
		return verifier.verify(token).getSubject();
	}
}
