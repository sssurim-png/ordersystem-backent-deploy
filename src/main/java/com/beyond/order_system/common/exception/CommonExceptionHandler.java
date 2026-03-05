package com.beyond.order_system.common.exception;

import com.beyond.order_system.common.dtos.CommonErrorDto;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Hidden // swagger에서 제외
public class CommonExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegal(IllegalArgumentException e) {
        e.printStackTrace();
        CommonErrorDto dto=CommonErrorDto.builder().statusCode(400).errorMessage(e.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFound(EntityNotFoundException e) {
        e.printStackTrace();
        CommonErrorDto dto=CommonErrorDto.builder().statusCode(400).errorMessage(e.getMessage()).build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }
}
