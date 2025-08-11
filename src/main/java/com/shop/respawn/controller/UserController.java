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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.shop.respawn.util.MaskingUtil.*;

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

    /**
     * 로그인 완료 처리
     */
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

    /**
     * 일반 유저 정보 조회
     */
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

    /**
     * 마이페이지에서 비밀번호가 일치하는지 검사하는 메서드
     */
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

    /**
     * 전화번호 수정 엔드포인트
     */
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

    /**
     * 비밀번호 변경 엔드포인트
     */
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

    /**
     * 1단계 - 이름 + 이메일 or 전화번호로 마스킹된 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody Map<String, String> response) {
        String name = response.get("name");
        String email = response.get("email");
        String phoneNumber = response.get("phoneNumber");

        if (phoneNumber == null && email != null) {
            String maskedUsername = userService.findMaskedUsernameByNameAndEmail(name, email);
            String findPhoneNumber = userService.findPhoneNumberByNameAndEmail(name, email);

            if (maskedUsername == null || findPhoneNumber == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(Map.of(
                    "maskedUsername", maskedUsername,
                    "email", maskEmail(email),
                    "phoneNumber", maskPhoneNumber(findPhoneNumber)
            ));
        } else if (email == null && phoneNumber != null) {
            String maskedUsername = userService.findMaskedUsernameByNameAndPhone(name, phoneNumber);
            String findEmail = userService.findEmailByNameAndPhone(name, phoneNumber);

            if (maskedUsername == null || findEmail == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
            }

            return ResponseEntity.ok(Map.of(
                    "maskedUsername", maskedUsername,
                    "phoneNumber", maskPhoneNumber(phoneNumber),
                    "email", maskEmail(findEmail)
            ));
        }

        return ResponseEntity.badRequest()
                .body(Map.of("error", "이메일 또는 전화번호 중 하나만 입력하세요."));
    }

    /**
     * 2단계 - 이메일로 실제 아이디 전송
     */
    @PostMapping("/find-id/email/send")
    public ResponseEntity<?> sendIdToEmail(@RequestBody Map<String, String> response) {
        userService.sendRealUsernameByEmail(response.get("name"), response.get("email"));
        return ResponseEntity.ok(Map.of("message", "아이디가 이메일로 전송되었습니다."));
    }

    /**
     * 2단계 - 전화번호로 실제 아이디 전송
     */
    @PostMapping("/find-id/phone/send")
    public ResponseEntity<?> sendIdToPhone(@RequestBody Map<String, String> response) {
        userService.sendRealUsernameByPhone(response.get("name"), response.get("phoneNumber"));
        return ResponseEntity.ok(Map.of("message", "아이디가 휴대폰으로 전송되었습니다."));
    }

    @PostMapping("/find-password/email")
    public ResponseEntity<Map<String, Object>> findPasswordByEmail(@RequestBody Map<String, String> request) {
        boolean result = userService.sendPasswordResetLinkByEmail(
                request.get("username"),
                request.get("name"),
                request.get("email")
        );
        if (result) {
            return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
        }
    }

    @PostMapping("/find-password/phone")
    public ResponseEntity<Map<String, Object>> findPasswordByPhone(@RequestBody Map<String, String> request) {
        boolean result = userService.sendPasswordResetLinkByPhone(
                request.get("username"),
                request.get("name"),
                request.get("phoneNumber")
        );
        if (result) {
            return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 휴대폰으로 전송되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
        }
    }

    /**
     * username 중복 체크
     */
    @GetMapping("signup/username/{username}")
    public Boolean checkUsernameDuplicate(@PathVariable String username) {
        return userService.checkUsernameDuplicate(username);
    }

    /**
     * phoneNumber 중복 체크
     */
    @GetMapping("signup/phoneNumber/{phoneNumber}")
    public Boolean checkPhoneNumberDuplicate(@PathVariable String phoneNumber) {
        return userService.checkPhoneNumberDuplicate(phoneNumber);
    }

    /**
     * email 중복 체크
     */
    @GetMapping("signup/email/{email}")
    public Boolean checkEmailDuplicate(@PathVariable String email) {
        return userService.checkEmailDuplicate(email);
    }

}
