package com.hello.boilerplate.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.user.presentation.dto.response.ProfileInfo;
import com.hello.boilerplate.user.presentation.dto.response.PublicProfileInfo;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	UserService userService;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Mock
	UserRepository userRepository;

	@Nested
	@DisplayName("회원 가입 기능")
	class UserRegisterTest {
		@Test
		void 유저_ID를_반환한다() {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			when(bCryptPasswordEncoder.encode(registerUser.password())).thenReturn(registerUser.password());

			User newUser = registerUser.toEntity(registerUser.password());
			ReflectionTestUtils.setField(newUser, "id", 1L);

			when(userRepository.save(any(User.class))).thenReturn(newUser);

			//when
			Long newUserId = userService.register(registerUser);

			//then
			assertThat(newUserId).isInstanceOf(Long.class);
		}

		@Test
		void 비밀번호를_인코딩한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			when(bCryptPasswordEncoder.encode(registerUser.password())).thenReturn(registerUser.password());
			User newUser = registerUser.toEntity(registerUser.password());
			ReflectionTestUtils.setField(newUser, "id", 1L);
			when(userRepository.save(any(User.class))).thenReturn(newUser);

		    //when
			userService.register(registerUser);

		    //then
			verify(bCryptPasswordEncoder, times(1)).encode(userFixture.getPassword());
		}

		@Test
		void 아이디_중복을_확인한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			String encodedPassword = "encodedPassword";
			when(bCryptPasswordEncoder.encode(registerUser.password())).thenReturn(encodedPassword);
			User newUser = registerUser.toEntity(encodedPassword);
			ReflectionTestUtils.setField(newUser, "id", 1L);

			when(userRepository.save(any(User.class))).thenReturn(newUser);
		    when(userRepository.existsByLoginId(userFixture.getLoginId())).thenReturn(false);

		    //when
			userService.register(registerUser);

		    //then
			verify(userRepository, times(1)).existsByLoginId(userFixture.getLoginId());
		}

		@Test
		void 아이디가_중복되면_예외를_반환한다() {
			//given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			when(userRepository.existsByLoginId(userFixture.getLoginId())).thenReturn(true);

			//when & then
			assertThatThrownBy(() -> userService.register(registerUser))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일_중복을_확인한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			String encodedPassword = "encodedPassword";
			when(bCryptPasswordEncoder.encode(registerUser.password())).thenReturn(encodedPassword);
			User newUser = registerUser.toEntity(encodedPassword);

			when(userRepository.save(any(User.class))).thenReturn(newUser);
		    when(userRepository.existsByEmail(userFixture.getEmail())).thenReturn(false);

		    //when
		    userService.register(registerUser);

		    //then
		    verify(userRepository, times(1)).existsByEmail(userFixture.getEmail());
		}

		@Test
		void 이메일이_중복되면_예외를_반환한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			when(userRepository.existsByEmail(userFixture.getEmail())).thenReturn(true);

		    //when & then
			assertThatThrownBy(() -> userService.register(registerUser))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 회원가입을_한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			String encodedPassword = "encodedPassword";
			when(bCryptPasswordEncoder.encode(registerUser.password())).thenReturn(encodedPassword);
			User newUser = registerUser.toEntity(encodedPassword);

			when(userRepository.save(any(User.class))).thenReturn(newUser);

		    //when
		    userService.register(registerUser);

		    //then
			verify(userRepository, times(1)).save(any(User.class));
		}
		
		@Test
		void 회원_저장_시_DB_유니크_제약조건으로_예외를_반환한다() {
		    //given
			User userFixture = UserFixture.USER_FIXTURE_1.create();
			RegisterUser registerUser = new RegisterUser(
				userFixture.getLoginId(),
				userFixture.getPassword(),
				userFixture.getEmail(),
				userFixture.getName()
			);
			when(userRepository.save(any(User.class)))
				.thenThrow(DataIntegrityViolationException.class);

		    //when & then
			assertThatThrownBy(() -> userService.register(registerUser))
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
			Mockito.when(userRepository.existsByLoginId(loginId)).thenReturn(true);

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.checkDuplicateLoginId(loginId))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 아이디_중복_체크를_진행한다() {
		    //given
			String loginId = "testLoginId";
			Mockito.when(userRepository.existsByLoginId(loginId)).thenReturn(false);

		    //when
		    userService.checkDuplicateLoginId(loginId);

		    //then
			Mockito.verify(userRepository, times(1)).existsByLoginId(loginId);
		}
	}

	@Nested
	@DisplayName("이메일 중복 체크 기능")
	class checkDuplicateEmail {
		@Test
		void 이메일이_중복이면_예외를_반환한다() {
		    //given
			String email = "test@test.com";
		    Mockito.when(userRepository.existsByEmail(email)).thenReturn(true);

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.checkDuplicateEmail(email))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 이메일_중복_체크를_한다() {
		    //given
		    String email = "test@test.com";
			Mockito.when(userRepository.existsByEmail(email)).thenReturn(false);

		    //when
		    userService.checkDuplicateEmail(email);

		    //then
		    verify(userRepository, times(1)).existsByEmail(email);
		}
	}

	@Nested
	@DisplayName("회원 프로필 조회")
	class GetProfileInfo {
		@Test
		void 프로필을_반환한다() {
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
	@DisplayName("공개 프로필 조회")
	class GetPublicProfileInfo {
		@Test
		void 회원_PK로_유저를_조회한다() {
		    //given
			Long userId = 1L;
			User user = UserFixture.USER_FIXTURE_1.create();
			Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

			//when
			userService.getPublicProfileInfo(userId);

		    //then
		    verify(userRepository, times(1)).findById(userId);
		}

		@Test
		void 회원_PK로_회원을_찾을_수_없다면_예외를_반환한다() {
		    //given
			Long userId = 1L;
			Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

		    //when & then
			Assertions.assertThatThrownBy(() -> userService.getPublicProfileInfo(userId))
				.isInstanceOf(NotFoundException.class);
		}

		@Test
		void 공개_프로필을_응답한다() {
		    //given
		    Long userId = 1L;
			User user = UserFixture.USER_FIXTURE_1.create();
			PublicProfileInfo publicProfileInfo = PublicProfileInfo.from(user);

			Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		    //when
			PublicProfileInfo result = userService.getPublicProfileInfo(userId);

			//then
		    Assertions.assertThat(result).isEqualTo(publicProfileInfo);
		}
	}

	@Nested
	@DisplayName("회원 정보 업데이트")
	class Update {

		@Test
		void 회원_정보를_업데이트한다() {
		    //given
			Long userId = 1L;
			User user = UserFixture.USER_FIXTURE_1.create();
			ReflectionTestUtils.setField(user, "id", userId);

			String changedName = "이름바꾸기";
			UpdateUser updateUser = new UpdateUser(changedName);

			//when
			Long result = userService.update(updateUser, user);

		    //then
			assertAll(
				() -> Assertions.assertThat(result).isEqualTo(userId),
				() -> Assertions.assertThat(user.getName()).isEqualTo(changedName)
			);
		}
	}

	@Nested
	@DisplayName("비밀 번호 업데이트 기능")
	class UpdatePassword {
		@Test
		void 기존_비밀번호와_불일치_시_예외를_반환한다() {
		    //given
			String newPassword = "newPassword1234@";
			User user = UserFixture.USER_FIXTURE_1.create();
			ChangePassword changePassword = new ChangePassword(user.getPassword(), newPassword);

			Mockito.when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);

		    //when & then
		    Assertions.assertThatThrownBy(() -> userService.updatePassword(changePassword, user))
				.isInstanceOf(CustomException.class);
		}

		@Test
		void 새로운_비밀번호로_업데이트_한다() {
		    //given
			String newPassword = "newPassword1234@";
			User user = UserFixture.USER_FIXTURE_1.create();
			ChangePassword changePassword = new ChangePassword(user.getPassword(), newPassword);

			Mockito.when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);
			Mockito.when(bCryptPasswordEncoder.encode(newPassword)).thenReturn(newPassword);

		    //when
		    userService.updatePassword(changePassword, user);

		    //then
			Assertions.assertThat(user.getPassword()).isEqualTo(newPassword);
		}
	}

	@Nested
	@DisplayName("회원 탈퇴 기능")
	class Withdraw {
		@Test
		void 회원_탈퇴를_한다() {
		    //given
			User user = UserFixture.USER_FIXTURE_1.create();

		    //when
			userService.withdraw(user);

		    //then
		    verify(userRepository, times(1)).delete(user);
		}
	}
}