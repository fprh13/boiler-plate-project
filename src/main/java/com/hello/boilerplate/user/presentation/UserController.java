package com.hello.boilerplate.user.presentation;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;

import com.hello.boilerplate.user.application.UserService;
import com.hello.boilerplate.user.domain.User;
import com.hello.boilerplate.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.common.presentation.dto.ApiResponse;
import com.hello.boilerplate.common.infrastructure.web.CookieUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	@Value("${cookie.name}")
	private String REFRESH_TOKEN_COOKIE_NAME;

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> register(@RequestBody @Valid RegisterUser registerUser) {
        return ResponseEntity.status(OK).body(ApiResponse.of(userService.register(registerUser)));
    }

	@GetMapping("/login-id/exists")
	public ResponseEntity<ApiResponse<Object>> checkDuplicateLoginId(@RequestParam String loginId) {
		userService.checkDuplicateLoginId(loginId);
		return ResponseEntity.status(OK).body(ApiResponse.of());
	}

	@GetMapping("/email/exists")
	public ResponseEntity<ApiResponse<Object>> checkDuplicateEmail(@RequestParam String email) {
		userService.checkDuplicateEmail(email);
		return ResponseEntity.status(OK).body(ApiResponse.of());
	}

	@GetMapping("/profile")
	public ResponseEntity<ApiResponse<Object>> getProfileInfo(User user) {
		return ResponseEntity.status(OK).body(ApiResponse.of(userService.getProfileInfo(user)));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse<Object>> getPublicProfileInfo(@PathVariable Long userId) {
		return ResponseEntity.status(OK).body(ApiResponse.of(userService.getPublicProfileInfo(userId)));
	}

	@PutMapping
	public ResponseEntity<ApiResponse<Object>> update(@RequestBody @Valid UpdateUser updateUser, User user) {
		return ResponseEntity.status(OK).body(ApiResponse.of(userService.update(updateUser, user)));
	}

	@PatchMapping("/password")
	public ResponseEntity<ApiResponse<Object>> updatePassword(@RequestBody @Valid ChangePassword changePassword, User user) {
		userService.updatePassword(changePassword, user);
		return ResponseEntity.status(OK).body(ApiResponse.of());
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Object>> withdraw(User user) {
		userService.withdraw(user);
		return ResponseEntity.status(OK)
			.header(SET_COOKIE, CookieUtil.ofExpired(REFRESH_TOKEN_COOKIE_NAME).toString())
			.body(ApiResponse.of());
	}
}
