package com.hello.boilerplate.auth.presentation;

import static com.hello.boilerplate.auth.infrastructure.jwt.JwtConstants.*;
import static org.springframework.http.HttpHeaders.*;

import com.hello.boilerplate.auth.application.AccountRecoveryService;
import com.hello.boilerplate.auth.presentation.dto.request.AuthenticateUserRequest;
import com.hello.boilerplate.auth.presentation.dto.request.ResetPasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.RetrieveLoginIdRequest;
import com.hello.boilerplate.auth.presentation.dto.request.RetrievePasswordRequest;
import com.hello.boilerplate.auth.presentation.dto.request.VerifyPasswordCodeRequest;
import com.hello.boilerplate.auth.presentation.dto.response.AuthenticateUserResponse;
import com.hello.boilerplate.auth.presentation.dto.response.VerifyPasswordCodeResponse;
import com.hello.boilerplate.auth.presentation.dto.response.ReissueTokenResponse;
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
	private final AccountRecoveryService accountRecoveryService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> authenticate(@RequestBody @Valid AuthenticateUserRequest authenticateUserRequest) {
        AuthenticateUserResponse authenticateUserResponse = authService.authenticate(authenticateUserRequest);

        ResponseCookie responseCookie = CookieUtil.of(
			REFRESH_TOKEN_COOKIE_NAME,
			authenticateUserResponse.refreshToken(),
			REFRESH_TOKEN_VALID_DAYS * 24 * 60 * 60
		);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + authenticateUserResponse.accessToken())
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
		ReissueTokenResponse reissueTokenResponse = authService.reissueToken(refreshToken);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, BEARER_PREFIX + reissueTokenResponse.accessToken())
                .body(ApiResponse.of());
    }

	@PostMapping("/id/find")
	public ResponseEntity<ApiResponse<Void>> retrieveLoginId(@RequestBody @Valid RetrieveLoginIdRequest retrieveLoginIdRequest) {
		accountRecoveryService.retrieveLoginId(retrieveLoginIdRequest);
		return ResponseEntity.ok().body(ApiResponse.of());
	}

	@PostMapping("/password/find")
	public ResponseEntity<ApiResponse<Void>> retrievePassword(@RequestBody @Valid RetrievePasswordRequest retrievePasswordRequest) {
		accountRecoveryService.retrievePassword(retrievePasswordRequest);
		return ResponseEntity.ok().body(ApiResponse.of());
	}

	@PostMapping("/password/verify")
	public ResponseEntity<ApiResponse<VerifyPasswordCodeResponse>> verifyCode(@RequestBody @Valid VerifyPasswordCodeRequest verifyPasswordCodeRequest) {
		return ResponseEntity.ok().body(ApiResponse.of(accountRecoveryService.verifyCode(verifyPasswordCodeRequest)));
	}

	@PostMapping("/password/reset")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest) {
		accountRecoveryService.resetPassword(resetPasswordRequest);
		return ResponseEntity.ok().body(ApiResponse.of());
	}
}
