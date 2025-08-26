package com.hello.boilerplate.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hello.boilerplate.global.dto.ResponseDto;

@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<ResponseDto<Void>> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getHttpStatus()).body(ResponseDto.fromErrorCode(errorCode));
	}
}
