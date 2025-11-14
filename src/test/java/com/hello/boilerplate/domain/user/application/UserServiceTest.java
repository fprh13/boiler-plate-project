package com.hello.boilerplate.domain.user.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.global.exception.CustomException;
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
}