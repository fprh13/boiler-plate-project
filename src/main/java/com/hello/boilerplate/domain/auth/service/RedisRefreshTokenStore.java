package com.hello.boilerplate.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore {
    private static final String REFRESH_PREFIX = "rt:";
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(final String subject, final String refreshToken, final long expirationSeconds) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + subject, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(final String subject) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + subject);
    }

    public Boolean deleteRefreshToken(final String subject) {
        return redisTemplate.delete(REFRESH_PREFIX + subject);
    }
}
