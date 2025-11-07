package com.hello.boilerplate.global.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hello.boilerplate.global.dto.ResponseDto;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
        if (e.getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error(e.getMessage());
        }
		return ResponseEntity.status(e.getStatus()).body(ResponseDto.ofFail(e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ResponseDto<Void>> handleValidationException(MethodArgumentNotValidException e) {
		return ResponseEntity.status(BAD_REQUEST).body(
                ResponseDto.ofFail(e.getBindingResult().getFieldErrors())
        );
	}
}
