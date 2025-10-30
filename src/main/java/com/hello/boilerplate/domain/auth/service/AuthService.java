package com.hello.boilerplate.domain.auth.service;

import com.hello.boilerplate.domain.auth.dto.LoginRequestDto;
import com.hello.boilerplate.domain.auth.dto.LoginResponseDto;
import com.hello.boilerplate.domain.auth.dto.ReissueResponseDto;
import com.hello.boilerplate.domain.auth.utils.JwtUtil;
import com.hello.boilerplate.domain.user.entity.User;
import com.hello.boilerplate.domain.user.repository.UserRepository;
import com.hello.boilerplate.global.exception.CustomException;
import com.hello.boilerplate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
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
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

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
                .orElseThrow(() -> new CustomException(ErrorCode.AUTHENTICATION_REQUIRED));

        Date now = new Date();
        return new ReissueResponseDto(jwtUtil.createAccessToken(user, now));
    }
}
