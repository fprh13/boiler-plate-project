package com.hello.boilerplate.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
    private static final String REFRESH_PREFIX = "rt:";
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(final String email, final String refreshToken, final long expirationSeconds) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + email, refreshToken, expirationSeconds, TimeUnit.SECONDS);
    }

    public String getRefreshToken(final String email) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
    }

    public Boolean deleteRefreshToken(final String email) {
        return redisTemplate.delete(REFRESH_PREFIX + email);
    }
}
