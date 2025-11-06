package com.hello.boilerplate.domain.user.controller;

import com.hello.boilerplate.domain.user.dto.UserRequestDto;
import com.hello.boilerplate.domain.user.service.UserService;
import com.hello.boilerplate.global.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ResponseDto<Object>> register(@RequestBody final UserRequestDto.Register request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDto.ofSuccess(HttpStatus.CREATED, userService.register(request)));
    }
}
