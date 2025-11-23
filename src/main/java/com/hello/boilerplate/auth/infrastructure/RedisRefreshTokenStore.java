package com.hello.boilerplate.auth.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.hello.boilerplate.auth.application.RefreshTokenStore;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private final long expirationSeconds;
    private final RedisTemplate<String, String> redisTemplate;

	public RedisRefreshTokenStore(
		RedisTemplate<String, String> redisTemplate,
		@Value("${jwt.refresh-token-valid-days}") Long expirationDays
	) {
		this.redisTemplate = redisTemplate;
		this.expirationSeconds = expirationDays * 24 * 60 * 60;
	}

	@Override
    public void save(String subject, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + subject, refreshToken, expirationSeconds, TimeUnit.SECONDS);
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
