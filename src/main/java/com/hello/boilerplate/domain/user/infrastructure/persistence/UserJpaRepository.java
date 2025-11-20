package com.hello.boilerplate.domain.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hello.boilerplate.domain.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

	Optional<User> findUserByLoginId(String loginId);
	boolean existsByLoginId(String loginId);
	boolean existsByEmail(String email);
}
