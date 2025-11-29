package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUserRequest;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticateUserResponse;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueTokenResponse;
import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtTokenProvider;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final static String MATCH_ERROR_MESSAGE = "아이디 혹은 비밀번호가 일치하지 않습니다.";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final RefreshTokenStore refreshTokenStore;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserResponse authenticate(AuthenticateUserRequest authenticateUserRequest) {
        User user = userRepository.findByLoginId(authenticateUserRequest.loginId())
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE));

		if (!bCryptPasswordEncoder.matches(authenticateUserRequest.password(), user.getPassword())) {
			throw new CustomException(HttpStatus.BAD_REQUEST, MATCH_ERROR_MESSAGE);
		}

        Date now = new Date();
		String accessToken = jwtTokenProvider.createAccessToken(user, now);
		String refreshToken = jwtTokenProvider.createRefreshToken(user, now);
		refreshTokenStore.save(user.getLoginId(), refreshToken);

        return new AuthenticateUserResponse(accessToken, refreshToken);
    }

    public void invalidate(String subject) {
		refreshTokenStore.delete(subject);
    }

    public ReissueTokenResponse reissueToken(String refreshToken) {
		String subject = jwtTokenProvider.parseRefreshToken(refreshToken).getSubject();
		validateRefreshToken(subject, refreshToken);

        User user = userRepository.findByLoginId(subject)
                .orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.AUTH_USER_NOT_FOUND));
        return new ReissueTokenResponse(jwtTokenProvider.createAccessToken(user, new Date()));
    }

	private void validateRefreshToken(String subject, String refreshToken) {
		String storedRefreshToken = refreshTokenStore.get(subject);

		if (!refreshToken.equals(storedRefreshToken)) {
			refreshTokenStore.delete(subject);
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}
}
