package com.hello.boilerplate.global.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ResponseDto<Void>> handleValidationException(MethodArgumentNotValidException e) {
		FieldError fieldError = e.getBindingResult()
			.getFieldErrors()
			.get(e.getBindingResult().getFieldErrors().size() - 1);
		return ResponseEntity.status(BAD_REQUEST).body(ResponseDto.fromFieldError(fieldError));
	}
}
