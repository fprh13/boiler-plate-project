package com.hello.boilerplate.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "email", length = 30, unique = true)
	private String email;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "provider", length = 100, unique = true)
	private String provider;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 20, nullable = false)
	private Role role;
}
