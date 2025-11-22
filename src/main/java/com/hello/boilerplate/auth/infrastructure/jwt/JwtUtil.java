package com.hello.boilerplate.auth.infrastructure.jwt;

import com.hello.boilerplate.auth.application.RefreshTokenStore;
import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
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
public class JwtUtil {

    private final RefreshTokenStore refreshTokenStore;
    private final SecretKey accessTokenSigningKey;
    private final SecretKey refreshTokenSigningKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public JwtUtil(
			RefreshTokenStore refreshTokenStore,
            @Value("${jwt.access-secret-key}") String accessTokenSecret,
            @Value("${jwt.refresh-secret-key}") String refreshTokenSecret,
            @Value("${jwt.access-token-valid}") Long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-valid}") Long refreshTokenExpirationSeconds) {
        this.refreshTokenStore = refreshTokenStore;
        this.accessTokenSigningKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSigningKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String createAccessToken(User user, Date now) {

        return Jwts.builder()
                .subject(user.getLoginId())
                .claim(JwtConstants.AUTHORITIES_KEY, user.getRole().getKey())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationSeconds * 1_000L))
                .signWith(accessTokenSigningKey)
                .compact();
    }

    public String createRefreshToken(User user, Date now) {

        String refreshToken = Jwts.builder()
                .subject(user.getLoginId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationSeconds * 1_000L))
                .signWith(refreshTokenSigningKey)
                .compact();

        refreshTokenStore.save(user.getLoginId(), refreshToken, refreshTokenExpirationSeconds);

        return refreshToken;
    }

    public void validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessTokenSigningKey)
                    .build()
                    .parseSignedClaims(token);

        } catch (JwtException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public void validateRefreshToken(String subject, String requestRefreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(refreshTokenSigningKey)
                    .build()
                    .parseSignedClaims(requestRefreshToken);

            String storedRefreshToken = refreshTokenStore.get(subject);

            if (!requestRefreshToken.equals(storedRefreshToken)) {
                invalidateRefreshToken(subject);
                throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);

            }

        } catch (JwtException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);

        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public Claims getAccessTokenClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(accessTokenSigningKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED);
        }
    }

    public void invalidateRefreshToken(String subject) {
        refreshTokenStore.delete(subject);
    }
}
