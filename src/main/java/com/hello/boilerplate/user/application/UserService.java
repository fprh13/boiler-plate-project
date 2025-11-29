package com.hello.boilerplate.user.application;

import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.user.presentation.dto.request.ChangePasswordRequest;
import com.hello.boilerplate.user.presentation.dto.request.RegisterUserRequest;
import com.hello.boilerplate.user.presentation.dto.request.UpdateUserRequest;
import com.hello.boilerplate.user.presentation.dto.response.UserProfileResponse;
import com.hello.boilerplate.user.presentation.dto.response.PublicUserProfileResponse;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
	private static final String LOGIN_ID_DUPLICATE_MESSAGE = "이미 사용중인 아이디입니다.";
	private static final String EMAIL_DUPLICATE_MESSAGE = "이미 사용중인 이메일입니다.";
	private static final String DUPLICATE_MESSAGE = "아이디 혹은 이메일이 이미 사용중입니다.";
	private static final String PASSWORD_MISMATCH_MESSAGE = "비밀번호가 일치하지 않습니다.";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long register(RegisterUserRequest registerUserRequest) {
		validateDuplicateLoginId(registerUserRequest.loginId());
		validateDuplicateEmail(registerUserRequest.email());

		String encodedPassword = bCryptPasswordEncoder.encode(registerUserRequest.password());
		try {
			return userRepository.save(registerUserRequest.toEntity(encodedPassword)).getId();
		} catch (DataIntegrityViolationException e) {
			throw new CustomException(HttpStatus.CONFLICT, DUPLICATE_MESSAGE);
		}
    }

	private void validateDuplicateLoginId(String loginId) {
		if (userRepository.existsByLoginId(loginId)) {
			throw new CustomException(HttpStatus.CONFLICT, LOGIN_ID_DUPLICATE_MESSAGE);
		}
	}

	private void validateDuplicateEmail(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(HttpStatus.CONFLICT, EMAIL_DUPLICATE_MESSAGE);
		}
	}

	public void checkDuplicateLoginId(String loginId) {
		validateDuplicateLoginId(loginId);
	}

	public void checkDuplicateEmail(String email) {
		validateDuplicateEmail(email);
	}

	public UserProfileResponse getProfileInfo(User user) {
		return UserProfileResponse.from(user);
	}

	public PublicUserProfileResponse getPublicProfileInfo(Long userId) {
		return userRepository.findById(userId)
			.map(PublicUserProfileResponse::from)
			.orElseThrow(() -> new NotFoundException(User.class));
	}

	@Transactional
	public Long update(UpdateUserRequest updateUserRequest, User user) {
		user.updateInfo(updateUserRequest.name());
		return user.getId();
	}

	@Transactional
	public void updatePassword(ChangePasswordRequest changePasswordRequest, User user) {
		if (!bCryptPasswordEncoder.matches(changePasswordRequest.password(), user.getPassword())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, PASSWORD_MISMATCH_MESSAGE);
		}
		user.updatePassword(bCryptPasswordEncoder.encode(changePasswordRequest.newPassword()));
	}

	@Transactional
	public void withdraw(User user) {
		user.withdraw();
		userRepository.delete(user);
	}
}
