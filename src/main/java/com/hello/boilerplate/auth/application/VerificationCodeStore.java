package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.infrastructure.verification.VerificationPurpose;

public interface VerificationCodeStore {
	void save(VerificationPurpose purpose, String key, String code);
	String get(VerificationPurpose purpose, String key);
	void delete(VerificationPurpose purpose, String key);
}
