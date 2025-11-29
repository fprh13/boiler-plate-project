package com.hello.boilerplate.auth.infrastructure.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.hello.boilerplate.auth.application.RefreshTokenStore;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private final long expirationSeconds;
    private final RedisTemplate<String, String> stringRedisTemplate;

	public RedisRefreshTokenStore(
		RedisTemplate<String, String> stringRedisTemplate,
		@Value("${jwt.refresh-token-valid-days}") Long expirationDays
	) {
		this.stringRedisTemplate = stringRedisTemplate;
		this.expirationSeconds = expirationDays * 24 * 60 * 60;
	}

	@Override
    public void save(String subject, String token) {
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + subject, token, expirationSeconds, TimeUnit.SECONDS);
    }

	@Override
    public String get(String subject) {
        return stringRedisTemplate.opsForValue().get(KEY_PREFIX + subject);
    }

	@Override
    public void delete(String subject) {
        stringRedisTemplate.delete(KEY_PREFIX + subject);
    }
}
