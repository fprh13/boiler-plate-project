package com.hello.boilerplate.auth.presentation;

import com.hello.boilerplate.auth.presentation.dto.request.LoginRequestDto;
import com.hello.boilerplate.auth.presentation.dto.response.LoginResponseDto;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueResponseDto;
import com.hello.boilerplate.auth.application.AuthService;
import com.hello.boilerplate.auth.infrastructure.jwt.JwtUtil;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.common.presentation.dto.ApiResponse;
import com.hello.boilerplate.common.infrastructure.web.CookieUtil;
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
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequestDto loginRequestDto) {

        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);

        ResponseCookie responseCookie =
                CookieUtil.of(REFRESH_TOKEN_COOKIE_NAME, loginResponseDto.refreshToken(), REFRESH_TOKEN_VALID_TIME);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, loginResponseDto.accessToken())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(ApiResponse.of());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(User user) {

        authService.logout(user.getLoginId());
        ResponseCookie responseExpiredCookie = CookieUtil.ofExpired(REFRESH_TOKEN_COOKIE_NAME);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseExpiredCookie.toString())
                .body(ApiResponse.of());
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissue(HttpServletRequest request, User user) {

        String refreshToken = CookieUtil.findCookieByName(request, REFRESH_TOKEN_COOKIE_NAME).toString();
        ReissueResponseDto reissueResponseDto = authService.reissue(user.getLoginId(), refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, reissueResponseDto.accessToken())
                .body(ApiResponse.of());
    }
}
