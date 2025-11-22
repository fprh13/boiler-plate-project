package com.hello.boilerplate.auth.application;

public interface RefreshTokenStore {
	String REFRESH_PREFIX = "rt:";

	void save(String subject, String token);
	String get(String subject);
	Boolean delete(String subject);
}
