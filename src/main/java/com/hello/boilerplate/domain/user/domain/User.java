package com.hello.boilerplate.domain.user.domain;

import com.hello.boilerplate.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity<User> {

    @Column(name = "login_id", length = 30, unique = true)
    private String loginId;

    @Column(name = "password", length = 200, nullable = false)
    private String password;

	@Column(name = "email", length = 30, unique = true)
	private String email;

	@Column(name = "name", length = 20, nullable = false)
	private String name;

	@Column(name = "provider", length = 100, unique = true)
	private String provider;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", length = 20, nullable = false)
	private Role role;

    public User(
            final String loginId,
            final String password,
            final String email,
            final String name,
            final Role role
    ) {
        this.loginId = loginId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
        registerEvent(new UserRegisteredEvent(this));
    }

	public void updateInfo(String name) {
		this.name = name;
	}

	public void updatePassword(String encodedNewPassword) {
		this.password = encodedNewPassword;
	}
}
