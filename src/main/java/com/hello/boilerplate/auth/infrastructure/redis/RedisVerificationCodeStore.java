package com.hello.boilerplate.auth.infrastructure.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.hello.boilerplate.auth.application.VerificationCodeStore;
import com.hello.boilerplate.auth.domain.VerificationPurpose;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisVerificationCodeStore implements VerificationCodeStore {

	private static final String KEY_PREFIX = "code:";
	private static final Long DEFAULT_TTL_SECONDS = 60L * 5;

	private final RedisTemplate<String, String> stringRedisTemplate;

	@Override
	public void save(VerificationPurpose purpose, String key, String code) {
		String redisKey = createKey(purpose, key);
		stringRedisTemplate.opsForValue().set(redisKey, code, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public String get(VerificationPurpose purpose, String key) {
		return stringRedisTemplate.opsForValue().get(createKey(purpose, key));
	}

	@Override
	public void delete(VerificationPurpose purpose, String key) {
		stringRedisTemplate.delete(createKey(purpose, key));
	}

	private String createKey(VerificationPurpose purpose, String key) {
		return KEY_PREFIX + purpose.name().toLowerCase() + ":" + key;
	}
}
