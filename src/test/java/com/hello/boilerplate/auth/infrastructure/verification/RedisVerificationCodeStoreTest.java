package com.hello.boilerplate.auth.infrastructure.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisVerificationCodeStoreTest {

	private static final String TEST_LOGIN_ID = "test1";
	private static final String TEST_CODE = "123456";
	private static final String TEST_CODE_KEY = "code:password_reset:test1";
	private static final Long EXPIRATION_SECONDS = 60L * 5;

	@InjectMocks RedisVerificationCodeStore redisVerificationCodeStore;
	@Mock RedisTemplate<String, String> stringRedisTemplate;
	@Mock ValueOperations<String, String> valueOperations;

	@Test
	void 인증번호를_저장한다() {
		// given
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		// when
		redisVerificationCodeStore.save(VerificationPurpose.PASSWORD_RESET, TEST_LOGIN_ID, TEST_CODE);

		// then
		verify(valueOperations, times(1))
			.set(TEST_CODE_KEY, TEST_CODE, EXPIRATION_SECONDS, TimeUnit.SECONDS);
	}

	@Test
	void 인증번호를_조회한다() {
		// given
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(TEST_CODE_KEY)).thenReturn(TEST_CODE);

		// when
		String result = redisVerificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, TEST_LOGIN_ID);

		// then
		assertThat(result).isEqualTo(TEST_CODE);
		verify(valueOperations, times(1)).get(TEST_CODE_KEY);
	}

	@Test
	void 인증번호가_조회되지_않는다면_null을_반환한다() {
		// given
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(TEST_CODE_KEY)).thenReturn(null);

		// when
		String resultRefreshToken = redisVerificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, TEST_LOGIN_ID);

		// then
		assertThat(resultRefreshToken).isNull();
		verify(valueOperations, times(1)).get(TEST_CODE_KEY);
	}

	@Test
	void 인증번호를_삭제한다() {
		// given
		when(stringRedisTemplate.delete(TEST_CODE_KEY)).thenReturn(true);

		// when
		redisVerificationCodeStore.delete(VerificationPurpose.PASSWORD_RESET, TEST_LOGIN_ID);

		// then
		verify(stringRedisTemplate, times(1)).delete(TEST_CODE_KEY);
	}

}