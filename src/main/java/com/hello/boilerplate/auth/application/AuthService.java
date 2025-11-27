package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.request.FindLoginId;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.common.exception.NotFoundException;
import com.hello.boilerplate.common.infrastructure.mail.MailSender;
import com.hello.boilerplate.common.infrastructure.mail.TemplateRenderer;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.domain.UserRepository;
import com.hello.boilerplate.common.exception.CustomException;
import com.hello.boilerplate.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final static String MATCH_ERROR_MESSAGE = "아이디 혹은 비밀번호가 일치하지 않습니다.";
	private static final String RETRIEVE_LOGIN_ID_MAIL_SUBJECT = "아이디 확인 메일";
	private static final String RETRIEVE_LOGIN_ID_MAIL = "mail/auth/retrieve-login-id";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final RefreshTokenStore refreshTokenStore;
    private final JwtUtil jwtUtil;
	private final MailSender mailSender;
	private final TemplateRenderer templateRenderer;

    public AuthenticationResult authenticate(AuthenticateUser authenticateUser) {
        User user = userRepository.findUserByLoginId(authenticateUser.loginId())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE));

		if (!bCryptPasswordEncoder.matches(authenticateUser.password(), user.getPassword())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE);
		}

        Date now = new Date();
		String accessToken = jwtUtil.createAccessToken(user, now);
		String refreshToken = jwtUtil.createRefreshToken(user, now);
		refreshTokenStore.save(user.getLoginId(), refreshToken);

        return new AuthenticationResult(accessToken, refreshToken);
    }

    public void invalidate(String subject) {
		refreshTokenStore.delete(subject);
    }

    public ReissuedToken reissueToken(String refreshToken) {
		String subject = jwtUtil.getRefreshTokenClaims(refreshToken).getSubject();
		validateRefreshToken(subject, refreshToken);

        User user = userRepository.findUserByLoginId(subject)
                .orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.AUTH_USER_NOT_FOUND));
        return new ReissuedToken(jwtUtil.createAccessToken(user, new Date()));
    }

	private void validateRefreshToken(String subject, String refreshToken) {
		String storedRefreshToken = refreshTokenStore.get(subject);

		if (!refreshToken.equals(storedRefreshToken)) {
			refreshTokenStore.delete(subject);
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}

	public void retrieveLoginId(FindLoginId findLoginId) {
		User user = userRepository.findUserByEmail(findLoginId.email())
			.orElseThrow(() -> new NotFoundException(User.class));

		String mailContent = templateRenderer.render(RETRIEVE_LOGIN_ID_MAIL,
			Map.of(
				"name", user.getName(),
				"loginId", user.getLoginId()
			)
		);
		mailSender.send(user.getEmail(), RETRIEVE_LOGIN_ID_MAIL_SUBJECT, mailContent);
	}
}
