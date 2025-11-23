package com.hello.boilerplate.common.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hello.boilerplate.common.presentation.dto.ApiResponse;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

	@GetMapping
	public ResponseEntity<ApiResponse<Object>> healthCheck() {
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.of());
	}
}