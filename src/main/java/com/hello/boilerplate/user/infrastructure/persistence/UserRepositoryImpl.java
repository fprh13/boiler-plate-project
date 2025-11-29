package com.hello.boilerplate.user.infrastructure.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public User save(User user) {
		return userJpaRepository.save(user);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userJpaRepository.findById(id);
	}

	@Override
	public void delete(User user) {
		userJpaRepository.delete(user);
	}

	@Override
	public Optional<User> findUserByLoginId(String loginId) {
		return userJpaRepository.findUserByLoginId(loginId);
	}

	@Override
	public Optional<User> findUserByEmail(String email) {
		return userJpaRepository.findUserByEmail(email);
	}

	@Override
	public boolean existsByLoginId(String loginId) {
		return userJpaRepository.existsByLoginId(loginId);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmail(email);
	}

	@Override
	public Optional<User> findUserByLoginIdAndEmail(String loginId, String email) {
		return userJpaRepository.findUserByLoginIdAndEmail(loginId, email);
	}
}
