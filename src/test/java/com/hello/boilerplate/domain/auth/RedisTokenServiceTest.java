package com.hello.boilerplate.domain.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RedisTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisTokenService redisTokenService;

    private static final String EMAIL = "test@gmail.com";
    private static final String REFRESH_TOKEN = "testRefreshToken";
    private static final String REFRESH_TOKEN_KEY = "rt:test@gmail.com";
    private static final long EXPIRATION_SECONDS = 1000L * 60 * 60 * 24;

    @Test
    void shouldSaveRefreshTokenWhenEmailAndTokenAndExpirationGiven() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        redisTokenService.saveRefreshToken(EMAIL, REFRESH_TOKEN, EXPIRATION_SECONDS);

        // then
        verify(valueOperations, times(1))
                .set(REFRESH_TOKEN_KEY, REFRESH_TOKEN, EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void shouldFindRefreshTokenWhenRedisKeyGiven() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(REFRESH_TOKEN);

        // when
        String resultRefreshToken = redisTokenService.getRefreshToken(EMAIL);

        // then
        assertThat(resultRefreshToken).isEqualTo(REFRESH_TOKEN);
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void shouldReturnNullFindRefreshTokenWithNonExistingKey() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        // when
        String resultRefreshToken = redisTokenService.getRefreshToken(EMAIL);

        // then
        assertThat(resultRefreshToken).isNull();
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void ShouldDeleteRefreshTokenWhenRedisKeyGiven() {
        // given
        when(redisTemplate.delete(REFRESH_TOKEN_KEY)).thenReturn(true);

        // when
        Boolean result = redisTokenService.deleteRefreshToken(EMAIL);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_KEY);
    }

    @Test
    void shouldReturnFalseWhenDeleteRefreshTokenFails() {
        // given
        when(redisTemplate.delete(REFRESH_TOKEN_KEY)).thenReturn(false);

        // when
        Boolean result = redisTokenService.deleteRefreshToken(EMAIL);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_KEY);
    }
}