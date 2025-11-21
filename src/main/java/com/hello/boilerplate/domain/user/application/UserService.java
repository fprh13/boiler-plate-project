package com.hello.boilerplate.domain.user.application;

import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.domain.UserRepository;
import com.hello.boilerplate.domain.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.domain.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.domain.user.presentation.dto.response.ProfileInfo;
import com.hello.boilerplate.domain.user.presentation.dto.response.PublicProfileInfo;
import com.hello.boilerplate.global.common.exception.CustomException;
import com.hello.boilerplate.global.common.exception.NotFoundException;

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
    public Long register(final RegisterUser registerUser) {
		validateDuplicateLoginId(registerUser.loginId());
		validateDuplicateEmail(registerUser.email());

		String encodedPassword = bCryptPasswordEncoder.encode(registerUser.password());
		try {
			return userRepository.save(registerUser.toEntity(encodedPassword)).getId();
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

	public ProfileInfo getProfileInfo(User user) {
		return ProfileInfo.from(user);
	}

	public PublicProfileInfo getPublicProfileInfo(Long userId) {
		return userRepository.findById(userId)
			.map(PublicProfileInfo::from)
			.orElseThrow(() -> new NotFoundException(User.class));
	}

	@Transactional
	public Long update(UpdateUser updateUser, User user) {
		user.updateInfo(updateUser.name());
		return user.getId();
	}

	@Transactional
	public void updatePassword(ChangePassword changePassword, User user) {
		if (!bCryptPasswordEncoder.matches(changePassword.password(), user.getPassword())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, PASSWORD_MISMATCH_MESSAGE);
		}
		user.updatePassword(bCryptPasswordEncoder.encode(changePassword.newPassword()));
	}

	@Transactional
	public void withdraw(User user) {
		user.withdraw();
		userRepository.delete(user);
	}
}
