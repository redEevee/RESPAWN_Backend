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
import java.util.UUID;

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

        String realUsername = null;
        String maskedUsername = null;
        String maskedEmail = null;
        String maskedPhone = null;
        Long userId = null;

        try {
            if (phoneNumber == null && email != null) {
                realUsername = userService.getRealUsernameByNameAndEmail(name, email); // 실제 아이디 조회 메서드 새로 만듦
                maskedUsername = maskMiddleFourChars(realUsername);
                String findPhoneNumber = userService.findPhoneNumberByNameAndEmail(name, email);
                if (findPhoneNumber == null) throw new RuntimeException();

                maskedEmail = maskEmail(email);
                maskedPhone = maskPhoneNumber(findPhoneNumber);
                userId = userService.getUserIdByUsername(realUsername);
            } else if (email == null && phoneNumber != null) {
                realUsername = userService.getRealUsernameByNameAndPhone(name, phoneNumber);
                maskedUsername = maskMiddleFourChars(realUsername);
                String findEmail = userService.findEmailByNameAndPhone(name, phoneNumber);
                if (findEmail == null) throw new RuntimeException();

                maskedPhone = maskPhoneNumber(phoneNumber);
                maskedEmail = maskEmail(findEmail);
                userId = userService.getUserIdByUsername(realUsername);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이메일 또는 전화번호 중 하나만 입력하세요."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
        }

        // 임시 토큰 생성 및 레디스 저장
        String token = UUID.randomUUID().toString();
        userService.storeUsernameToken(token, realUsername); // userService에서 Redis에 저장하는 메서드 호출

        return ResponseEntity.ok(Map.of(
                "maskedUsername", maskedUsername,
                "email", maskedEmail,
                "phoneNumber", maskedPhone,
                "token", token,
                "userId", userId
        ));
    }

    /**
     * 2단계 - 이메일 or 전화번호로 실제 아이디 전송
     */
    @PostMapping("/find-id/send")
    public ResponseEntity<?> sendId(
            @RequestBody Map<String, String> response) {

        String token = response.get("token");
        Long userId;
        String type = response.get("type"); // "email" 또는 "phone"

        if (token == null || type == null || response.get("userId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "필수 정보를 입력하세요."));
        }

        try {
            userId = Long.valueOf(response.get("userId"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId 형식이 올바르지 않습니다."));
        }

        // Redis 토큰으로 username 조회
        String realUsername = userService.getUsernameByToken(token);
        if (realUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "유효하지 않거나 만료된 토큰입니다."));
        }

        // DB에서 회원정보 조회
        String name;
        String email = null;
        String phoneNumber = null;

        Buyer buyer = buyerRepository.findById(userId).orElse(null);
        if (buyer != null) {
            name = buyer.getName();
            email = buyer.getEmail();
            phoneNumber = buyer.getPhoneNumber();
        } else {
            Seller seller = sellerRepository.findById(userId).orElse(null);
            if (seller != null) {
                name = seller.getName();
                email = seller.getEmail();
                phoneNumber = seller.getPhoneNumber();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "해당 회원을 찾을 수 없습니다."));
            }
        }

        if ("email".equalsIgnoreCase(type)) {
            // 이름 + 이메일 검증
            if (!userService.verifyUsernameNameEmail(realUsername, name, email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "입력 정보와 토큰이 일치하지 않습니다."));
            }
            // 이메일 발송
            userService.sendRealUsernameByEmail(name, email);

        } else if ("phone".equalsIgnoreCase(type)) {
            // 이름 + 전화번호 검증
            if (!userService.verifyUsernameNamePhone(realUsername, name, phoneNumber)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "입력 정보와 토큰이 일치하지 않습니다."));
            }
            // 문자 발송
            userService.sendRealUsernameByPhone(name, phoneNumber);

        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "type은 'email' 또는 'phone' 이어야 합니다."));
        }

        // 사용 후 토큰 삭제
        userService.deleteUsernameToken(token);

        return ResponseEntity.ok(Map.of("message", "아이디가 " + ("email".equalsIgnoreCase(type) ? "이메일" : "휴대폰") + "으로 전송되었습니다."));
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
