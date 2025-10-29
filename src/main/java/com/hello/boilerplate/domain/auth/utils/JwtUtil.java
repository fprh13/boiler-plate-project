package com.hello.boilerplate.domain.auth.utils;

import com.hello.boilerplate.domain.auth.service.RedisTokenService;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final RedisTokenService redisTokenService;
    private final SecretKey accessTokenSigningKey;
    private final SecretKey refreshTokenSigningKey;
    private final long accessTokenExpirationSeconds;
    private final long refreshTokenExpirationSeconds;

    public static final String AUTHORITIES_KEY = "role";

    public JwtUtil(
            RedisTokenService redisTokenService,
            @Value("${jwt.access-secret-key}") String accessTokenSecret,
            @Value("${jwt.refresh-secret-key}") String refreshTokenSecret,
            @Value("${jwt.access-token-valid}") Long accessTokenExpirationSeconds,
            @Value("${jwt.refresh-token-valid}") Long refreshTokenExpirationSeconds) {
        this.redisTokenService = redisTokenService;
        this.accessTokenSigningKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenSigningKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
    }

    public String createAccessToken(User user, Date now) {

        return Jwts.builder()
                .subject(user.getLoginId())
                .claim(AUTHORITIES_KEY, user.getRole().getKey())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationSeconds))
                .signWith(accessTokenSigningKey)
                .compact();
    }

    public String createRefreshToken(User user, Date now) {

        String refreshToken = Jwts.builder()
                .subject(user.getLoginId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationSeconds))
                .signWith(refreshTokenSigningKey)
                .compact();

        redisTokenService.saveRefreshToken(user.getLoginId(), refreshToken, refreshTokenExpirationSeconds);

        return refreshToken;
    }

    public void validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessTokenSigningKey)
                    .build()
                    .parseSignedClaims(token);

        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    public void validateRefreshToken(String subject, String requestRefreshToken) {
        try {
            Jwts.parser()
                    .verifyWith(refreshTokenSigningKey)
                    .build()
                    .parseSignedClaims(requestRefreshToken);

            String storedRefreshToken = redisTokenService.getRefreshToken(subject);

            if (!requestRefreshToken.equals(storedRefreshToken)) {
                invalidateRefreshToken(subject);
                throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
            }

        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
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
            throw new CustomException(ErrorCode.INVALID_AUTH_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.AUTHENTICATION_REQUIRED);
        }
    }

    public void invalidateRefreshToken(String subject) {
        redisTokenService.deleteRefreshToken(subject);
    }
}
