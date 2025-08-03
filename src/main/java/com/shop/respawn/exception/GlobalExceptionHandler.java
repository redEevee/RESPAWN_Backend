package com.shop.respawn.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Map<String, String> handleBadRequest(Exception ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleNotFound(Exception ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxSizeException() {
        return ResponseEntity
                .badRequest()
                .body("업로드 파일 크기가 너무 큽니다. 제한 용량 이내로 파일을 업로드해 주세요.");
    }
}
