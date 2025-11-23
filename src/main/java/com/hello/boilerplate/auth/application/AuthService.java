package com.hello.boilerplate.auth.application;

import com.hello.boilerplate.auth.presentation.dto.request.LoginRequestDto;
import com.hello.boilerplate.auth.presentation.dto.response.LoginResponseDto;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueResponseDto;
import com.hello.boilerplate.auth.exception.AuthorizationErrorMessages;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
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

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final RefreshTokenStore refreshTokenStore;
    private final JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findUserByLoginId(loginRequestDto.loginId())
                .filter(u -> bCryptPasswordEncoder.matches(loginRequestDto.password(), u.getPassword()))
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다."));

        Date now = new Date();
		String accessToken = jwtUtil.createAccessToken(user, now);
		String refreshToken = jwtUtil.createRefreshToken(user, now);
		refreshTokenStore.save(user.getLoginId(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public void logout(String subject) {
		refreshTokenStore.delete(subject);
    }

    public ReissueResponseDto reissue(String subject, String refreshToken) {
		validateRefreshToken(subject, refreshToken);

        User user = userRepository.findUserByLoginId(subject)
                .orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.AUTH_USER_NOT_FOUND));

        Date now = new Date();
        return new ReissueResponseDto(jwtUtil.createAccessToken(user, now));
    }

	private void validateRefreshToken(String subject, String refreshToken) {
		String storedRefreshToken = refreshTokenStore.get(subject);

		if (!refreshToken.equals(storedRefreshToken)) {
			refreshTokenStore.delete(subject);
			throw new UnauthorizedException(AuthorizationErrorMessages.INVALID_TOKEN_EXCEPTION);
		}
	}
}
