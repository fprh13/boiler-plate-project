package com.hello.boilerplate.auth.application;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.auth.infrastructure.verification.VerificationPurpose;
import com.hello.boilerplate.auth.presentation.dto.request.ResetPasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest;
import com.hello.boilerplate.auth.presentation.dto.request.RetrievePasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.VerifyPasswordCodeRequest;
import com.hello.boilerplate.auth.presentation.dto.response.VerifyPasswordCodeResponse;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountRecoveryService {
	private static final String VERIFY_CODE_ERROR_MESSAGE = "인증번호가 올바르지 않습니다.";

	private static final String RETRIEVE_LOGIN_ID_MAIL_SUBJECT = "아이디 확인 메일";
	private static final String RETRIEVE_LOGIN_ID_MAIL = "mail/auth/retrieve-login-id";
	private static final String RETRIEVE_PASSWORD_MAIL_SUBJECT = "인증번호 발송 메일";
	private static final String RETRIEVE_PASSWORD_MAIL = "mail/auth/retrieve-password";

	private final UserRepository userRepository;
	private final MailSender mailSender;
	private final TemplateRenderer templateRenderer;
	private final VerificationCodeStore verificationCodeStore;
	private final JwtUtil jwtUtil;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public void retrieveLoginId(RetrieveLoginIdRequest retrieveLoginIdRequest) {
		User user = userRepository.findByEmail(retrieveLoginIdRequest.email())
			.orElseThrow(() -> new NotFoundException(User.class));

		String mailContent = templateRenderer.render(RETRIEVE_LOGIN_ID_MAIL,
			Map.of(
				"name", user.getName(),
				"loginId", user.getLoginId()
			)
		);
		mailSender.send(user.getEmail(), RETRIEVE_LOGIN_ID_MAIL_SUBJECT, mailContent);
	}

	public void retrievePassword(RetrievePasswordRequest retrievePasswordRequest) {
		User user = userRepository.findByLoginIdAndEmail(retrievePasswordRequest.loginId(), retrievePasswordRequest.email())
			.orElseThrow(() -> new NotFoundException(User.class));

		String code = generateCode();
		verificationCodeStore.save(VerificationPurpose.PASSWORD_RESET, user.getLoginId(), code);
		String mailContent = templateRenderer.render(RETRIEVE_PASSWORD_MAIL, Map.of("code", code));

		mailSender.send(user.getEmail(), RETRIEVE_PASSWORD_MAIL_SUBJECT, mailContent);
	}

	private String generateCode() {
		int number = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
		return String.valueOf(number);
	}

	public VerifyPasswordCodeResponse verifyCode(VerifyPasswordCodeRequest verifyPasswordCodeRequest) {
		String loginId = verifyPasswordCodeRequest.loginId();
		String storeCode = verificationCodeStore.get(VerificationPurpose.PASSWORD_RESET, loginId);

		if (!storeCode.equals(verifyPasswordCodeRequest.code())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, VERIFY_CODE_ERROR_MESSAGE);
		}
		verificationCodeStore.delete(VerificationPurpose.PASSWORD_RESET, loginId);

		return new VerifyPasswordCodeResponse(
			jwtUtil.createVerificationToken(VerificationPurpose.PASSWORD_RESET, loginId, new Date())
		);
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
		String loginId = jwtUtil.parseVerificationToken(
			VerificationPurpose.PASSWORD_RESET, resetPasswordRequest.token()
		).getSubject();

		User user = userRepository.findByLoginId(loginId)
			.orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.AUTH_USER_NOT_FOUND));
		user.updatePassword(bCryptPasswordEncoder.encode(resetPasswordRequest.password()));
	}
}
