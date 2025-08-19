package com.shop.respawn.controller;

import com.shop.respawn.dto.user.BuyerListDto;
import com.shop.respawn.dto.user.SellerListDto;
import com.shop.respawn.dto.user.UserSummaryDto;
import com.shop.respawn.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 사용자 계정 활성화
     */
    @PostMapping("/{userType}/{userId}/enable")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> enable(@PathVariable String userType,
                                                      @PathVariable Long userId) {
        adminService.enableUserById(userType, userId);
        boolean enabled = adminService.isEnabledById(userType, userId);
        return ResponseEntity.ok(Map.of(
                "userType", userType,
                "userId", userId,
                "enabled", enabled,
                "message", "계정이 활성화되었습니다."
        ));
    }

    /**
     * 사용자 계정 비활성화(정지)
     */
    @PostMapping("/{userType}/{userId}/disable")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> disable(@PathVariable String userType,
                                                       @PathVariable Long userId) {
        adminService.disableUserById(userType, userId);
        boolean enabled = adminService.isEnabledById(userType, userId);
        return ResponseEntity.ok(Map.of(
                "userType", userType,
                "userId", userId,
                "enabled", enabled,
                "message", "계정이 정지되었습니다."
        ));
    }

    /**
     * 사용자 계정 정지 해제
     */
    @PostMapping("/{userType}/{userId}/unlock")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> unlock(@PathVariable String userType,
                                                      @PathVariable Long userId) {
        adminService.unlockById(userType, userId);
        return ResponseEntity.ok(Map.of(
                "userType", userType,
                "userId", userId,
                "message", "계정 잠금이 해제되었습니다."
        ));
    }

    /**
     * 사용자 계정 상태 조회
     */
    @GetMapping("/{userType}/{userId}")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String userType,
                                                      @PathVariable Long userId) {
        boolean enabled = adminService.isEnabledById(userType, userId);
        return ResponseEntity.ok(Map.of(
                "userType", userType,
                "userId", userId,
                "enabled", enabled
        ));
    }

    // ----- 구매자 조회 -----
    @GetMapping("/buyers/paged")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Page<BuyerListDto>> getBuyersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(adminService.findBuyersPaged(page, size, sort, dir));
    }

    // ----- 판매자 조회 -----
    @GetMapping("/sellers/paged")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<Page<SellerListDto>> getSellersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(adminService.findSellersPaged(page, size, sort, dir));
    }

    @GetMapping("/{userType}/{userId}/summary")
    @Secured("ROLE_ADMIN")
    public ResponseEntity<UserSummaryDto> getUserSummary(
            @PathVariable String userType,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminService.findUserSummaryById(userType, userId));
    }
}
