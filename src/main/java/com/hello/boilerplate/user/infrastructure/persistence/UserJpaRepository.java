package com.hello.boilerplate.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hello.boilerplate.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

	Optional<User> findByLoginId(String loginId);
	Optional<User> findByEmail(String email);
	boolean existsByLoginId(String loginId);
	boolean existsByEmail(String email);
	Optional<User> findByLoginIdAndEmail(String loginId, String email);
}
