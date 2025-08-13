package com.hello.boilerplate.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hello.boilerplate.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
