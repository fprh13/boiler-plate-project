package com.hello.boilerplate.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import com.hello.boilerplate.auth.application.RefreshTokenStore;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

	@Override
    public void save(final String subject, final String refreshToken, final long expirationSeconds) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + subject, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

	@Override
    public String get(final String subject) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + subject);
    }

	@Override
    public Boolean delete(final String subject) {
        return redisTemplate.delete(REFRESH_PREFIX + subject);
    }
}
