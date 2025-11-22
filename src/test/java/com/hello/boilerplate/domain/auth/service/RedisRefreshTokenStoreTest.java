package com.hello.boilerplate.domain.auth.service;

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
class RedisRefreshTokenStoreTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRefreshTokenStore redisRefreshTokenStore;

    private static final String SUBJECT = "test@gmail.com";
    private static final String REFRESH_TOKEN = "testRefreshToken";
    private static final String REFRESH_TOKEN_KEY = "rt:test@gmail.com";
    private static final long EXPIRATION_SECONDS = 1000L * 60 * 60 * 24;

    @Test
    void 재발급_토큰을_저장한다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        redisRefreshTokenStore.saveRefreshToken(SUBJECT, REFRESH_TOKEN, EXPIRATION_SECONDS);

        // then
        verify(valueOperations, times(1))
                .set(REFRESH_TOKEN_KEY, REFRESH_TOKEN, EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void 재발급_토큰을_조회한다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(REFRESH_TOKEN);

        // when
        String resultRefreshToken = redisRefreshTokenStore.getRefreshToken(SUBJECT);

        // then
        assertThat(resultRefreshToken).isEqualTo(REFRESH_TOKEN);
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void 재발급_토큰이_조회되지_않는다면_null을_반환한다() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        // when
        String resultRefreshToken = redisRefreshTokenStore.getRefreshToken(SUBJECT);

        // then
        assertThat(resultRefreshToken).isNull();
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void 재발급_토큰을_삭제한다() {
        // given
        when(redisTemplate.delete(REFRESH_TOKEN_KEY)).thenReturn(true);

        // when
        Boolean result = redisRefreshTokenStore.deleteRefreshToken(SUBJECT);

        // then
        assertThat(result).isTrue();
        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_KEY);
    }

    @Test
    void 재발급_토큰_삭제에_실패하면_false를_반환한다() {
        // given
        when(redisTemplate.delete(REFRESH_TOKEN_KEY)).thenReturn(false);

        // when
        Boolean result = redisRefreshTokenStore.deleteRefreshToken(SUBJECT);

        // then
        assertThat(result).isFalse();
        verify(redisTemplate, times(1)).delete(REFRESH_TOKEN_KEY);
    }
}