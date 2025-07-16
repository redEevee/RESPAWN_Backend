package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.dto.BuyerDto;
import com.shop.respawn.service.BuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody BuyerDto buyerDto) {
        System.out.println("회원가입 컨트롤러 실행" + buyerDto);
        buyerService.join(buyerDto);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/loginOk")
    public ResponseEntity<Map<String, String>> loginOk() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        System.out.println("로그인한 유저네임:" + username);
        System.out.println("유저 권한:" + authentication.getAuthorities());

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("authorities", authorities);

        ResponseEntity<Map<String, String>> ok = ResponseEntity.ok(userInfo);
        System.out.println("ok = " + ok);
        return ok;
    }

    @GetMapping("/logoutOk")
    public ResponseEntity<?> logoutOk() {
        System.out.println("로그아웃 성공");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminPage() {
        System.out.println("어드민 인증 성공");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<Buyer> getUserPage() {
        System.out.println("일반 인증 성공");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 유저 정보
        Buyer buyer = buyerService.getBuyerInfo(username);

        return ResponseEntity.ok(buyer);
    }

    @GetMapping("buyers/signup/username/{username}")
    public Boolean checkUsernameDuplicate(@PathVariable String username) {
        return buyerService.checkUsernameDuplicate(username);
    }

    @GetMapping("buyers/signup/phoneNumber/{phoneNumber}")
    public Boolean checkPhoneNumberDuplicate(@PathVariable String phoneNumber) {
        return buyerService.checkPhoneNumberDuplicate(phoneNumber);
    }

    @GetMapping("buyers/signup/email/{email}")
    public Boolean checkEmailDuplicate(@PathVariable String email) {
        return buyerService.checkEmailDuplicate(email);
    }

}
