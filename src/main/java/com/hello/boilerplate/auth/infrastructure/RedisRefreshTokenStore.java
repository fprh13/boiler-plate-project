package com.hello.boilerplate.auth.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.hello.boilerplate.auth.application.RefreshTokenStore;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private final long expirationSeconds;
    private final RedisTemplate<String, String> tokenRedisTemplate;

	public RedisRefreshTokenStore(
		RedisTemplate<String, String> tokenRedisTemplate,
		@Value("${jwt.refresh-token-valid-days}") Long expirationDays
	) {
		this.tokenRedisTemplate = tokenRedisTemplate;
		this.expirationSeconds = expirationDays * 24 * 60 * 60;
	}

	@Override
    public void save(String subject, String token) {
        tokenRedisTemplate.opsForValue().set(KEY_PREFIX + subject, token, expirationSeconds, TimeUnit.SECONDS);
    }

	@Override
    public String get(String subject) {
        return tokenRedisTemplate.opsForValue().get(KEY_PREFIX + subject);
    }

	@Override
    public void delete(String subject) {
        tokenRedisTemplate.delete(KEY_PREFIX + subject);
    }
}
