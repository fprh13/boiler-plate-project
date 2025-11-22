package com.hello.boilerplate.auth.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.hello.boilerplate.auth.application.RefreshTokenStore;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private final long refreshTokenExpirationSeconds;
    private final RedisTemplate<String, String> redisTemplate;

	public RedisRefreshTokenStore(RedisTemplate<String, String> redisTemplate,
		@Value("${jwt.refresh-token-valid}") Long refreshTokenExpirationSeconds) {
		this.redisTemplate = redisTemplate;
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
	}

	@Override
    public void save(String subject, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + subject, refreshToken, refreshTokenExpirationSeconds * 1_000L, TimeUnit.SECONDS);
    }

	@Override
    public String get(String subject) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + subject);
    }

	@Override
    public Boolean delete(String subject) {
        return redisTemplate.delete(REFRESH_PREFIX + subject);
    }
}
