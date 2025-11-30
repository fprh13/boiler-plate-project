package com.hello.boilerplate.user.integration;

import com.hello.boilerplate.user.application.UserService;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.user.presentation.dto.request.ChangePasswordRequest;
import com.hello.boilerplate.user.presentation.dto.request.RegisterUserRequest;
import com.hello.boilerplate.user.presentation.dto.request.UpdateUserRequest;
import com.hello.boilerplate.user.presentation.dto.response.UserProfileResponse;
import com.hello.boilerplate.user.presentation.dto.response.PublicUserProfileResponse;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.support.fixture.UserFixture;
import com.hello.boilerplate.support.IntegrationSupportTest;

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
			RegisterUserRequest requestDto = new RegisterUserRequest(
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

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				loginId,
				"test1@1234",
				"test1@test.com",
				"홍길동"
			);
			userService.register(otherRequestDto);

			RegisterUserRequest requestDto = new RegisterUserRequest(
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

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
				"testUser1",
				"test1@1234",
				email,
				"홍길동"
			);
			userService.register(otherRequestDto);

			RegisterUserRequest requestDto = new RegisterUserRequest(
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

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
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

			RegisterUserRequest otherRequestDto = new RegisterUserRequest(
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
	class GetUserProfileResponse {
		@Test
		void 프로필을_응답한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();
			UserProfileResponse userProfileResponse = UserProfileResponse.from(user);

			//when
			UserProfileResponse result = userService.getProfileInfo(user);

			//then
			assertThat(result).isEqualTo(userProfileResponse);
		}
	}

	@Nested
	@DisplayName("공개 프로필 조회 기능")
	class GetPublicUserProfileResponse {
		@Test
		void 공개_프로필을_응답한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();

			User user = userRepository.save(userFixture);
			PublicUserProfileResponse publicUserProfileResponse = PublicUserProfileResponse.from(user);

			//when
			PublicUserProfileResponse result = userService.getPublicProfileInfo(user.getId());

			//then
			Assertions.assertThat(result).isEqualTo(publicUserProfileResponse);

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
			UpdateUserRequest updateUserRequest = new UpdateUserRequest(changedName);

			//when
			Long userId = userService.update(updateUserRequest, user);

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
			ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(userFixture.getPassword(), newPassword);

			ReflectionTestUtils.setField(userFixture, "password", bCryptPasswordEncoder.encode(userFixture.getPassword()));
			User user = userRepository.save(userFixture);

		    //when
			userService.updatePassword(changePasswordRequest, user);

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