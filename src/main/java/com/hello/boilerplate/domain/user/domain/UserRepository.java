package com.hello.boilerplate.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hello.boilerplate.domain.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLoginId(String loginId);
}
