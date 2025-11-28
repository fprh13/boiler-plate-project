package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.infrastructure.verification.VerificationCodeType;

public interface VerificationCodeStore {
	void save(VerificationCodeType type, String key, String code);
	String get(VerificationCodeType type, String key);
	void delete(VerificationCodeType type, String key);
}
