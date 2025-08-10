package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.UserDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<Map<String, String>> loginOk(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        Buyer buyer = buyerRepository.findByUsername(username);
        String name = null;
        Long userId = null;
        if (buyer != null) {
            name = buyer.getName();
            userId = buyer.getId();
        } else {
            Seller seller = sellerRepository.findByUsername(username);
            if (seller != null) {
                name = seller.getName();
                userId = seller.getId();
            }
        }

        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);

        System.out.println("로그인한 유저네임:" + username);
        System.out.println("유저 권한:" + authentication.getAuthorities());
        System.out.println("userId = " + userId);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", name);
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
    public ResponseEntity<?> getUserPage() {
        System.out.println("일반 인증 성공");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // User 먼저 조회
        Buyer buyer = userService.getBuyerInfo(username);
        Seller seller = userService.getSellerInfo(username);

        if (buyer != null) {
            buyer.renewExpiryDate();
            buyerRepository.save(buyer);
            return ResponseEntity.ok(
                    new UserDto(buyer.getName(), buyer.getUsername(), buyer.getEmail(), buyer.getPhoneNumber(), buyer.getProvider(), buyer.getRole())
            );
        } else if (seller != null) {
            seller.renewExpiryDate();
            sellerRepository.save(seller);
            return ResponseEntity.ok(
                    new UserDto(seller.getName(), seller.getUsername(), seller.getEmail(), seller.getPhoneNumber(), seller.getRole())
            );
        }
        // 사용자 없을 경우
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/myPage/checkPassword")
    public ResponseEntity<Boolean> checkPassword(@RequestBody Map<String, String> request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        String inputPassword = request.get("password");

        Buyer buyer = buyerRepository.findByUsername(username);
        Seller seller = sellerRepository.findByUsername(username);

        String encodedPassword = null;

        if (buyer != null) {
            encodedPassword = buyer.getPassword();
        } else if (seller != null) {
            encodedPassword = seller.getPassword();
        }

        if (encodedPassword == null || inputPassword == null) {
            return ResponseEntity.ok(false);
        }

        boolean match = userService.passwordMatches(inputPassword, encodedPassword);
        return ResponseEntity.ok(match);
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

    // 비밀번호 변경 엔드포인트
    @PutMapping("/myPage/setPassword")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request
    ) {
        String username = authentication.getName();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "값이 비어 있습니다."));
        }

        boolean changed = userService.changePassword(username, currentPassword, newPassword);
        if (changed) {
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "현재 비밀번호가 일치하지 않습니다."));
        }
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
