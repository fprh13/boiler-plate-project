package com.hello.boilerplate.auth.infrastructure.verification;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.hello.boilerplate.auth.application.VerificationCodeStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisVerificationCodeStore implements VerificationCodeStore {

	private static final String KEY_PREFIX = "code:";
	private static final Long DEFAULT_TTL_SECONDS = 60L * 5;

	private final RedisTemplate<String, String> stringRedisTemplate;

	@Override
	public void save(VerificationCodeType type, String key, String code) {
		String redisKey = createKey(type, key);
		stringRedisTemplate.opsForValue().set(redisKey, code, DEFAULT_TTL_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public String get(VerificationCodeType type, String key) {
		return stringRedisTemplate.opsForValue().get(createKey(type, key));
	}

	@Override
	public void delete(VerificationCodeType type, String key) {
		stringRedisTemplate.delete(createKey(type, key));
	}

	private String createKey(VerificationCodeType type, String key) {
		return KEY_PREFIX + type.name().toLowerCase() + ":" + key;
	}
}
