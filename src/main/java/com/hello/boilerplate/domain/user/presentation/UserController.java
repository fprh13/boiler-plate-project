package com.hello.boilerplate.domain.user.presentation;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpStatus.*;

import com.hello.boilerplate.domain.user.application.UserService;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.presentation.dto.request.ChangePassword;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.domain.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.global.dto.SuccessResponseDto;
import com.hello.boilerplate.global.infrastructure.CookieUtil;

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
    public ResponseEntity<SuccessResponseDto<Object>> register(@RequestBody @Valid final RegisterUser registerUser) {
        return ResponseEntity.status(OK).body(SuccessResponseDto.of(userService.register(registerUser)));
    }

	@GetMapping("/login-id/exists")
	public ResponseEntity<SuccessResponseDto<Object>> checkDuplicateLoginId(@RequestParam String loginId) {
		userService.checkDuplicateLoginId(loginId);
		return ResponseEntity.status(OK).body(SuccessResponseDto.of());
	}

	@GetMapping("/email/exists")
	public ResponseEntity<SuccessResponseDto<Object>> checkDuplicateEmail(@RequestParam String email) {
		userService.checkDuplicateEmail(email);
		return ResponseEntity.status(OK).body(SuccessResponseDto.of());
	}

	@GetMapping("/profile")
	public ResponseEntity<SuccessResponseDto<Object>> getProfileInfo(User user) {
		return ResponseEntity.status(OK).body(SuccessResponseDto.of(userService.getProfileInfo(user)));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<SuccessResponseDto<Object>> getPublicProfileInfo(@PathVariable Long userId) {
		return ResponseEntity.status(OK).body(SuccessResponseDto.of(userService.getPublicProfileInfo(userId)));
	}

	@PutMapping
	public ResponseEntity<SuccessResponseDto<Object>> update(@RequestBody @Valid UpdateUser updateUser, User user) {
		return ResponseEntity.status(OK).body(SuccessResponseDto.of(userService.update(updateUser, user)));
	}

	@PatchMapping("/password")
	public ResponseEntity<SuccessResponseDto<Object>> updatePassword(@RequestBody @Valid ChangePassword changePassword, User user) {
		userService.updatePassword(changePassword, user);
		return ResponseEntity.status(OK).body(SuccessResponseDto.of());
	}

	@DeleteMapping
	public ResponseEntity<SuccessResponseDto<Object>> withdraw(User user) {
		userService.withdraw(user);
		return ResponseEntity.status(OK)
			.header(SET_COOKIE, CookieUtil.ofExpired(REFRESH_TOKEN_COOKIE_NAME).toString())
			.body(SuccessResponseDto.of());
	}
}
