package com.hello.boilerplate.auth.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

	private static final String SUBJECT = "test@gmail.com";
	private static final String REFRESH_TOKEN = "testRefreshToken";
	private static final String REFRESH_TOKEN_KEY = "rt:test@gmail.com";
	private static final long EXPIRATION_SECONDS = 21L * 24 * 60 * 60;

	private RedisRefreshTokenStore redisRefreshTokenStore;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
    private RedisTemplate<String, String> stringRedisTemplate;

	@BeforeEach
	void setUp() {
		Long expirationDays = 21L;
		redisRefreshTokenStore = new RedisRefreshTokenStore(stringRedisTemplate, expirationDays);
	}



    @Test
    void 재발급_토큰을_저장한다() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        redisRefreshTokenStore.save(SUBJECT, REFRESH_TOKEN);

        // then
        verify(valueOperations, times(1))
                .set(REFRESH_TOKEN_KEY, REFRESH_TOKEN, EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void 재발급_토큰을_조회한다() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(REFRESH_TOKEN);

        // when
        String resultRefreshToken = redisRefreshTokenStore.get(SUBJECT);

        // then
        assertThat(resultRefreshToken).isEqualTo(REFRESH_TOKEN);
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void 재발급_토큰이_조회되지_않는다면_null을_반환한다() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_KEY)).thenReturn(null);

        // when
        String resultRefreshToken = redisRefreshTokenStore.get(SUBJECT);

        // then
        assertThat(resultRefreshToken).isNull();
        verify(valueOperations, times(1)).get(REFRESH_TOKEN_KEY);
    }

    @Test
    void 재발급_토큰을_삭제한다() {
        // given
        when(stringRedisTemplate.delete(REFRESH_TOKEN_KEY)).thenReturn(true);

        // when
        redisRefreshTokenStore.delete(SUBJECT);

        // then
        verify(stringRedisTemplate, times(1)).delete(REFRESH_TOKEN_KEY);
    }
}