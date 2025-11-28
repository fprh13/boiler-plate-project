package com.hello.boilerplate.user.domain;

import java.util.Optional;

public interface UserRepository {
	User save(User user);
	Optional<User> findById(Long id);
	void delete(User user);

    Optional<User> findUserByLoginId(String loginId);
	Optional<User> findUserByEmail(String email);
	boolean existsByLoginId(String loginId);
	boolean existsByEmail(String email);
	Optional<User> findUserByLoginIdAndEmail(String loginId, String email);
}
