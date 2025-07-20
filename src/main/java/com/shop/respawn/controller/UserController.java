package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.UserDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    @PostMapping("/join/{userType}")
    public ResponseEntity<?> join(@RequestBody UserDto userDto) {
        System.out.println("회원가입 컨트롤러 실행" + userDto);
        userService.join(userDto);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/loginOk")
    public ResponseEntity<Map<String, String>> loginOk() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        Buyer buyer = buyerRepository.findByUsername(username);
        String name = null;
        if (buyer != null) {
            name = buyer.getName();
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                name = seller.getName();
            }
        }

        System.out.println("로그인한 유저네임:" + username);
        System.out.println("유저 권한:" + authentication.getAuthorities());

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", name != null ? name : "Unknown");
        userInfo.put("username", username);
        userInfo.put("authorities", authorities);

        ResponseEntity<Map<String, String>> ok = ResponseEntity.ok(userInfo);
        System.out.println("ok = " + ok);
        return ok;
    }

    @GetMapping("/logoutOk")
    public ResponseEntity<?> logoutOk() {
        System.out.println("로그아웃 성공");
        ResponseEntity<Object> build = ResponseEntity.ok().build();
        System.out.println("build = " + build);
        return build;
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminPage() {
        System.out.println("어드민 인증 성공");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserPage() {
        System.out.println("일반 인증 성공");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 구매자 먼저 조회
        Buyer buyer = userService.getBuyerInfo(username);
        if (buyer != null) {
            return ResponseEntity.ok(buyer);
        }

        // 판매자 조회
        Seller seller = userService.getSellerInfo(username);
        if (seller != null) {
            return ResponseEntity.ok(seller);
        }

        // 사용자 없을 경우
        return ResponseEntity.notFound().build();
    }

    // 전화번호 수정 엔드포인트
    @PutMapping("/myPage/setPhoneNumber")
    public Map<String, String> updatePhoneNumber(Authentication authentication,
                                                 @RequestBody Map<String, String> request) {
        String username = authentication.getName(); // 현재 로그인한 사용자 아이디 조회
        String newPhoneNumber = request.get("phoneNumber");

        if (newPhoneNumber == null || newPhoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호를 입력하세요.");
        }

        userService.updatePhoneNumber(username, newPhoneNumber);

        return Map.of("message", "전화번호가 성공적으로 변경되었습니다.");
    }

    @GetMapping("signup/username/{username}")
    public Boolean checkUsernameDuplicate(@PathVariable String username) {
        return userService.checkUsernameDuplicate(username);
    }

    @GetMapping("signup/phoneNumber/{phoneNumber}")
    public Boolean checkPhoneNumberDuplicate(@PathVariable String phoneNumber) {
        return userService.checkPhoneNumberDuplicate(phoneNumber);
    }

    @GetMapping("signup/email/{email}")
    public Boolean checkEmailDuplicate(@PathVariable String email) {
        return userService.checkEmailDuplicate(email);
    }

}
