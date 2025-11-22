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
    private final JwtUtil jwtUtil;

    public LoginResponseDto login(final LoginRequestDto loginRequestDto) {
        User user = userRepository.findUserByLoginId(loginRequestDto.loginId())
                .filter(u -> bCryptPasswordEncoder.matches(loginRequestDto.password(), u.getPassword()))
                .orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, "아이디 혹은 비밀번호가 일치하지 않습니다."));

        Date now = new Date();
        return new LoginResponseDto(
                jwtUtil.createAccessToken(user, now),
                jwtUtil.createRefreshToken(user, now)
        );
    }

    public void logout(final String subject) {
        jwtUtil.invalidateRefreshToken(subject);
    }

    public ReissueResponseDto reissue(final String subject, final String refreshToken) {
        jwtUtil.validateRefreshToken(subject, refreshToken);
        User user = userRepository.findUserByLoginId(subject)
                .orElseThrow(() -> new UnauthorizedException(AuthorizationErrorMessages.PERMISSION_DENIED));

        Date now = new Date();
        return new ReissueResponseDto(jwtUtil.createAccessToken(user, now));
    }
}
