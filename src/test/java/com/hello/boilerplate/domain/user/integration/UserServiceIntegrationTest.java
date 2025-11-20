package com.hello.boilerplate.domain.user.integration;

import com.hello.boilerplate.domain.user.application.UserService;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.domain.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.domain.user.presentation.dto.response.ProfileInfo;
import com.hello.boilerplate.domain.user.presentation.dto.response.PublicProfileInfo;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.global.exception.NotFoundException;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.module.IntegrationSupportTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class UserServiceIntegrationTest extends IntegrationSupportTest {

	private static final String USER_NOT_SAVED_MESSAGE = "회원이 저장되지 않았습니다.";

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
				.orElseThrow(() -> new AssertionError(USER_NOT_SAVED_MESSAGE));

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

	@Nested
	@DisplayName("아이디 중복 체크 기능")
	class checkDuplicateLoginId {
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
			userRepository.save(otherRequestDto.toEntity(bCryptPasswordEncoder.encode(otherRequestDto.password())));

		    //when & then
		    Assertions.assertThatThrownBy(() -> userService.checkDuplicateLoginId(loginId))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 아이디_중복을_확인한다() {
		    //given
			String loginId = "testLoginId";

		    //when & then
		    Assertions.assertThatNoException()
				.isThrownBy(() -> userService.checkDuplicateLoginId(loginId));
		}
	}

	@Nested
	@DisplayName("이메일 중복 체크 기능")
	class checkDuplicateEmail {
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
			userRepository.save(otherRequestDto.toEntity(bCryptPasswordEncoder.encode(otherRequestDto.password())));

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.checkDuplicateEmail(email))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일_중복을_확인한다() {
		    //given
			String email = "test@test.com";

		    //when & then
			Assertions.assertThatNoException()
				.isThrownBy(() -> userService.checkDuplicateEmail(email));
		}
	}

	@Nested
	@DisplayName("프로필 조회 기능")
	class GetProfileInfo {
		@Test
		void 프로필을_응답한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			ProfileInfo profileInfo = ProfileInfo.from(user);

			//when
			ProfileInfo result = userService.getProfileInfo(user);

			//then
			assertThat(result).isEqualTo(profileInfo);
		}
	}

	@Nested
	@DisplayName("공개 프로필 조회 기능")
	class GetPublicProfileInfo {
		@Test
		void 공개_프로필을_응답한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();

			User user = userRepository.save(userFixture);
			PublicProfileInfo publicProfileInfo = PublicProfileInfo.from(user);

			//when
			PublicProfileInfo result = userService.getPublicProfileInfo(user.getId());

			//then
			Assertions.assertThat(result).isEqualTo(publicProfileInfo);

		}

		@Test
		void 회원을_찾지_못하면_예외를_반환한다() {
		    //given
			Long userId = 1L;

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.getPublicProfileInfo(userId))
				.isInstanceOf(NotFoundException.class);
		}
	}

	@Nested
	@DisplayName("회원 정보 업데이트 기능")
	class Update {
		@Test
		void 회원_정보를_업데이트_한다() {
		    //given
			User user = userRepository.save(UserFixture.USER_FIXTURE_1.create());

			String changedName = "이름바꾸기";
			UpdateUser updateUser = new UpdateUser(changedName);

			//when
			Long userId = userService.update(updateUser, user);

			//then
			User result = userRepository.findById(userId)
				.orElseThrow(() -> new AssertionError(USER_NOT_SAVED_MESSAGE));
			Assertions.assertThat(result.getName()).isEqualTo(changedName);
		}
	}

	@Nested
	@DisplayName("비밀번호 업데이트 기능")
	class UpdatePassword {
		@Test
		void 새로운_비밀번호로_업데이트한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			String newPassword = "newPassword1234@";
			ChangePassword changePassword = new ChangePassword(userFixture.getPassword(), newPassword);

			ReflectionTestUtils.setField(userFixture, "password", bCryptPasswordEncoder.encode(userFixture.getPassword()));
			User user = userRepository.save(userFixture);

		    //when
			userService.updatePassword(changePassword, user);

		    //then
			User result = userRepository.findById(user.getId())
				.orElseThrow(() -> new AssertionError(USER_NOT_SAVED_MESSAGE));

			Assertions.assertThat(bCryptPasswordEncoder.matches(newPassword, result.getPassword())).isTrue();
		}
	}

	@Nested
	@DisplayName("회원 탈퇴 기능")
	class Withdraw {
		@Test
		void 회원_탈퇴를_한다() {
		    //given
			User user = userRepository.save(UserFixture.USER_FIXTURE_1.create());

		    //when
			userService.withdraw(user);

		    //then
			Assertions.assertThat(userRepository.findById(user.getId())).isEmpty();
		}
	}
}