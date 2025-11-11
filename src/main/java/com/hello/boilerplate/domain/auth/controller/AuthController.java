package com.hello.boilerplate.domain.auth.controller;

import com.hello.boilerplate.domain.auth.dto.LoginRequestDto;
import com.hello.boilerplate.domain.auth.dto.LoginResponseDto;
import com.hello.boilerplate.domain.auth.dto.ReissueResponseDto;
import com.hello.boilerplate.domain.auth.service.AuthService;
import com.hello.boilerplate.domain.auth.utils.JwtUtil;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.global.dto.SuccessResponseDto;
import com.hello.boilerplate.domain.auth.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auths")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-valid}")
    private Long REFRESH_TOKEN_VALID_TIME;

    @Value("${cookie.name}")
    private String REFRESH_TOKEN_COOKIE_NAME;

    @PostMapping("/login")
    public ResponseEntity<SuccessResponseDto<Void>> login(@RequestBody final LoginRequestDto loginRequestDto) {

        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);

        ResponseCookie responseCookie =
                CookieUtil.of(REFRESH_TOKEN_COOKIE_NAME, loginResponseDto.refreshToken(), REFRESH_TOKEN_VALID_TIME);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, loginResponseDto.accessToken())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(SuccessResponseDto.of());
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponseDto<Void>> logout(final User user) {

        authService.logout(user.getLoginId());
        ResponseCookie responseExpiredCookie = CookieUtil.ofExpired(REFRESH_TOKEN_COOKIE_NAME);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseExpiredCookie.toString())
                .body(SuccessResponseDto.of());
    }

    @PostMapping("/reissue")
    public ResponseEntity<SuccessResponseDto<Void>> reissue(final HttpServletRequest request, final User user) {

        String refreshToken = CookieUtil.findCookieByName(request, REFRESH_TOKEN_COOKIE_NAME).toString();
        ReissueResponseDto reissueResponseDto = authService.reissue(user.getLoginId(), refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, reissueResponseDto.accessToken())
                .body(SuccessResponseDto.of());
    }
}
