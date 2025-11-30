package com.hello.boilerplate.auth.infrastructure.jwt;

import com.hello.boilerplate.auth.domain.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.domain.VerificationPurpose;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
	private static final long MILLIS_PER_SECOND = 1_000L;
	private static final long VERIFICATION_TOKEN_EXPIRATION_SECONDS = 60L * 10;

    private final SecretKey accessTokenSigningKey;
    private final SecretKey refreshTokenSigningKey;
	private final SecretKey verifyTokenSigningKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtTokenProvider(
		@Value("${jwt.access-secret-key}") String accessTokenSecret,
		@Value("${jwt.refresh-secret-key}") String refreshTokenSecret,
		@Value("${jwt.verification-secret-key}") String verifyTokenSecretKey,
		@Value("${jwt.access-token-valid-days}") Long accessTokenExpirationDays,
		@Value("${jwt.refresh-token-valid-days}") Long refreshTokenExpirationDays
	) {
        this.accessTokenSigningKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSigningKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
		this.verifyTokenSigningKey = Keys.hmacShaKeyFor(verifyTokenSecretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationDays * 24 * 60 * 60;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationDays * 24 * 60 * 60;
    }

    public String createAccessToken(User user, Date now) {
        return Jwts.builder()
                .subject(user.getLoginId())
                .claim(JwtConstants.AUTHORITIES_CLAIM_KEY, user.getRole().getKey())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationSeconds * MILLIS_PER_SECOND))
                .signWith(accessTokenSigningKey)
                .compact();
    }

    public String createRefreshToken(User user, Date now) {
        return Jwts.builder()
                .subject(user.getLoginId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationSeconds * MILLIS_PER_SECOND))
                .signWith(refreshTokenSigningKey)
                .compact();
    }

	public String createVerificationToken(VerificationPurpose purpose, String loginId, Date now) {
		return Jwts.builder()
			.subject(loginId)
			.claim(JwtConstants.VERIFICATION_CLAIM_KEY, purpose.name())
			.issuedAt(now)
			.expiration(new Date(now.getTime() + VERIFICATION_TOKEN_EXPIRATION_SECONDS * MILLIS_PER_SECOND))
			.signWith(verifyTokenSigningKey)
			.compact();
	}

	public Claims parseAccessToken(String token) {
		Claims claims = parseClaims(token, accessTokenSigningKey);
		validateAccessTokenClaims(claims);
		return claims;
	}

	public Claims parseRefreshToken(String token) {
		Claims claims = parseClaims(token, refreshTokenSigningKey);
		validateRefreshTokenClaims(claims);
		return claims;
	}

	public Claims parseVerificationToken(VerificationPurpose purpose, String token) {
		Claims claims = parseClaims(token, verifyTokenSigningKey);
		validateVerificationTokenClaims(purpose, claims);
		return claims;
	}

	private Claims parseClaims(String token, SecretKey signingKey) {
		try {
			return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (JwtException | IllegalArgumentException e) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}

	private void validateAccessTokenClaims(Claims claims) {
		String subject = claims.getSubject();
		String role = claims.get(JwtConstants.AUTHORITIES_CLAIM_KEY, String.class);

		if (subject == null || role == null) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}

	private void validateRefreshTokenClaims(Claims claims) {
		String subject = claims.getSubject();
		if (subject == null) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}

	private void validateVerificationTokenClaims(VerificationPurpose requiredPurpose, Claims claims) {
		String subject = claims.getSubject();
		String purpose = claims.get(JwtConstants.VERIFICATION_CLAIM_KEY, String.class);

		if (subject == null || !requiredPurpose.name().equals(purpose)) {
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}
}
