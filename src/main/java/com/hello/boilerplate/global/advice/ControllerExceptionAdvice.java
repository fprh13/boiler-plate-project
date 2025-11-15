package com.hello.boilerplate.global.advice;

import static org.springframework.http.HttpStatus.*;

import com.hello.boilerplate.global.dto.ErrorResponseDto;
import com.hello.boilerplate.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionAdvice {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponseDto<Void>> handleCustomException(CustomException e) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error(e.getMessage());
        }
		return ResponseEntity.status(e.getStatus()).body(ErrorResponseDto.of(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto<Void>> handleValidationException(MethodArgumentNotValidException e) {
		return ResponseEntity.status(BAD_REQUEST).body(
                ErrorResponseDto.of(e.getBindingResult().getFieldErrors())
        );
	}
}
