package com.shop.respawn.controller;


import com.shop.respawn.exception.ApiException;
import com.shop.respawn.exception.ApiResponse;
import com.shop.respawn.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExController {

    @GetMapping("/api/exception/{id}")
    public ResponseEntity<ApiResponse<ExceptionDto>> getMember(@PathVariable("id") String id) {

        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }
        if (id.equals("bad")) {
            throw new ApiException(ErrorCode.NOT_FOUND_EXCEPTION);
        }
        if (id.equals("user-ex")) {
            throw new ApiException(ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        return ResponseEntity.ok(ApiResponse.success(new ExceptionDto(id, "hello " + id)));
    }

    @Data
    @AllArgsConstructor
    static class ExceptionDto {

        private String id;
        private String username;
    }
}
