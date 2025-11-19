package com.hello.boilerplate.domain.user.presentation;

import com.hello.boilerplate.domain.user.application.UserService;
import com.hello.boilerplate.domain.user.domain.User;
import com.hello.boilerplate.domain.user.presentation.dto.request.RegisterUser;
import com.hello.boilerplate.domain.user.presentation.dto.request.UpdateUser;
import com.hello.boilerplate.global.dto.SuccessResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<SuccessResponseDto<Object>> register(@RequestBody @Valid final RegisterUser registerUser) {
        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponseDto.of(userService.register(registerUser)));
    }

	@GetMapping("/login-id/exists")
	public ResponseEntity<SuccessResponseDto<Object>> checkDuplicateLoginId(@RequestParam String loginId) {
		userService.checkDuplicateLoginId(loginId);
		return ResponseEntity.status(HttpStatus.OK).body(SuccessResponseDto.of());
	}

	@GetMapping("/email/exists")
	public ResponseEntity<SuccessResponseDto<Object>> checkDuplicateEmail(@RequestParam String email) {
		userService.checkDuplicateEmail(email);
		return ResponseEntity.status(HttpStatus.OK).body(SuccessResponseDto.of());
	}

	@GetMapping("/profile")
	public ResponseEntity<SuccessResponseDto<Object>> getProfileInfo(User user) {
		return ResponseEntity.status(HttpStatus.OK).body(SuccessResponseDto.of(userService.getProfileInfo(user)));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<SuccessResponseDto<Object>> getPublicProfileInfo(@PathVariable Long userId) {
		return ResponseEntity.status(HttpStatus.OK).body(SuccessResponseDto.of(userService.getPublicProfileInfo(userId)));
	}

	@PutMapping
	public ResponseEntity<SuccessResponseDto<Object>> update(@RequestBody @Valid UpdateUser updateUser, User user) {
		return ResponseEntity.status(HttpStatus.OK)
			.body(SuccessResponseDto.of(userService.update(updateUser, user)));
	}
}
