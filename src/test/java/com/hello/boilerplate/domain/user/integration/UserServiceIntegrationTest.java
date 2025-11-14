package com.hello.boilerplate.domain.user.integration;

import com.hello.boilerplate.domain.user.application.UserService;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.IntegrationSupportTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceIntegrationTest extends IntegrationSupportTest {

    @Autowired
	UserService userService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Nested
	@DisplayName("회원 가입 기능")
	class UserRegisterTest {
		@Test
		void 회원가입을_한다() {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser requestDto = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			//when
			Long userId = userService.register(requestDto);

			//then
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new AssertionError("회원이 저장되지 않았습니다."));

			assertAll(
				() -> assertThat(user.getLoginId()).isEqualTo(requestDto.loginId()),
				() -> assertThat(user.getEmail()).isEqualTo(requestDto.email()),
				() -> assertThat(user.getName()).isEqualTo(requestDto.name()),
				() -> assertThat(bCryptPasswordEncoder.matches(requestDto.password(), user.getPassword())).isTrue()
			);
		}

		@Test
		void 아이디가_중복이면_예외를_반환한다() {
		    //given
			String loginId = "testLoginId";

			RegisterUser otherRequestDto = new RegisterUser(
				loginId,
				"test1@1234",
				"test1@test.com",
				"홍길동"
			);
			userService.register(otherRequestDto);

			RegisterUser requestDto = new RegisterUser(
				loginId,
				"test2@1234",
				"test2@test.com",
				"아이디중복유저"
			);

			//when & then
			Assertions.assertThatThrownBy(() -> userService.register(requestDto))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일이_중복이면_예외를_반환한다() {
			//given
			String email = "test@test.com";

			RegisterUser otherRequestDto = new RegisterUser(
				"testUser1",
				"test1@1234",
				email,
				"홍길동"
			);
			userService.register(otherRequestDto);

			RegisterUser requestDto = new RegisterUser(
				"testUser2",
				"test2@1234",
				email,
				"이메일중복유저"
			);

			//when & then
			Assertions.assertThatThrownBy(() -> userService.register(requestDto))
				.isInstanceOf(CustomException.class);
		}
	}
}