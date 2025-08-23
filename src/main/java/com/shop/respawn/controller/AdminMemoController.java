package com.shop.respawn.controller;

import com.shop.respawn.dto.adminMemo.AdminMemoResponse;
import com.shop.respawn.dto.adminMemo.AdminMemoUpsertRequest;
import com.shop.respawn.service.AdminMemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/memo")
@RequiredArgsConstructor
@Secured("ROLE_ADMIN")
public class AdminMemoController {

    private final AdminMemoService memoService;

    // 사용자당 1개: 없으면 생성, 있으면 수정
    @PostMapping("/upsert")
    public AdminMemoResponse upsert(@RequestBody AdminMemoUpsertRequest request) {
        return memoService.upsert(request);
    }

    // 단일 사용자 메모 조회
    @GetMapping
    public AdminMemoResponse get(
            @RequestParam String userType,
            @RequestParam Long userId
    ) {
        return memoService.getByUser(userType, userId);
    }
}