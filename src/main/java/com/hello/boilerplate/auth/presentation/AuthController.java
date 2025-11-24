package com.hello.boilerplate.auth.presentation;

import static com.hello.boilerplate.auth.infrastructure.jwt.JwtConstants.*;
import static org.springframework.http.HttpHeaders.*;

import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUser;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticationResult;
import com.hello.boilerplate.auth.presentation.dto.response.ReissuedToken;
import com.hello.boilerplate.auth.application.AuthService;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.common.presentation.dto.ApiResponse;
import com.hello.boilerplate.common.infrastructure.web.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	@Value("${jwt.refresh-token-valid-days}")
	private Long REFRESH_TOKEN_VALID_DAYS;

	@Value("${cookie.name}")
	private String REFRESH_TOKEN_COOKIE_NAME;

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> authenticate(@RequestBody @Valid AuthenticateUser authenticateUser) {
        AuthenticationResult authenticationResult = authService.authenticate(authenticateUser);

        ResponseCookie responseCookie = CookieUtil.of(
			REFRESH_TOKEN_COOKIE_NAME,
			authenticationResult.refreshToken(),
			REFRESH_TOKEN_VALID_DAYS * 24 * 60 * 60
		);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + authenticationResult.accessToken())
                .header(SET_COOKIE, responseCookie.toString())
                .body(ApiResponse.of());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> invalidate(User user) {

        authService.invalidate(user.getLoginId());
        ResponseCookie responseExpiredCookie = CookieUtil.ofExpired(REFRESH_TOKEN_COOKIE_NAME);

        return ResponseEntity.ok()
                .header(SET_COOKIE, responseExpiredCookie.toString())
                .body(ApiResponse.of());
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<Void>> reissueToken(HttpServletRequest request) {

		String refreshToken = CookieUtil.findCookieByName(request, REFRESH_TOKEN_COOKIE_NAME).getValue();
		ReissuedToken reissuedToken = authService.reissueToken(refreshToken);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + reissuedToken.accessToken())
                .body(ApiResponse.of());
    }
}
