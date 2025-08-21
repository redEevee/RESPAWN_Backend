package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.user.LoginOkResponse;
import com.shop.respawn.dto.user.UserDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.security.auth.PrincipalDetails;
import com.shop.respawn.service.UserService;
import com.shop.respawn.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.shop.respawn.util.MaskingUtil.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final RedisUtil redisUtil;

    @PostMapping("/join/{userType}")
    public ResponseEntity<?> join(@RequestBody UserDto userDto) {
        userService.join(userDto);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @GetMapping("//bring-me")
    public ResponseEntity<LoginOkResponse> bringMe(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        LoginOkResponse data = userService.getUserData(authorities, username);
        if (data == null /* 또는 data.isEmpty() 등 도메인 규칙 */) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(data);
    }

    /**
     * 로그인 완료 처리
     */
    @GetMapping("/loginOk")
    public ResponseEntity<LoginOkResponse> loginOk(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("username = " + username);
        String authorities = authentication.getAuthorities().toString();
        System.out.println("authorities = " + authorities);

        LoginOkResponse loginOkResponse = userService.getUserData(authorities, username);

        HttpSession session = request.getSession();
        session.setAttribute("userId", loginOkResponse.getUserId());
        System.out.println("session.getId() = " + session.getId());
        System.out.println("loginOkResponse = " + loginOkResponse);

        return ResponseEntity.ok(loginOkResponse);
    }

    @GetMapping("/logoutOk")
    public ResponseEntity<?> logoutOk() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAdminPage() {
        return ResponseEntity.ok().build();
    }

    /**
     * 일반 유저 정보 조회
     */
    @GetMapping("/user")
    public ResponseEntity<UserDto> getUserPage(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserInfo(authentication.getName()));
    }

    /**
     * 마이페이지에서 비밀번호가 일치하는지 검사하는 메서드
     */
    @PostMapping("/myPage/checkPassword")
    public ResponseEntity<Boolean> checkPassword(Authentication authentication,
                                                 @RequestBody Map<String, String> password) {
        return ResponseEntity.ok(userService.isMatchPassword(authentication.getName(), password.get("password")));
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
     * 이름 + 이메일 or 전화번호로 마스킹된 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody Map<String, String> response) {
        String name = response.get("name");
        String email = response.get("email");
        String phoneNumber = response.get("phoneNumber");

        String realUsername;
        String maskedUsername;
        String maskedEmail;
        String maskedPhone;
        Long userId;

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
     * 이메일 or 전화번호로 실제 아이디 전송 컨트롤러
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
        String email;
        String phoneNumber;

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

    /**
     * 이메일 or 전화번호 사용 비밀번호 찾을 계정 조회 컨트롤러
     */
    @PostMapping("/find-password")
    public ResponseEntity<Map<String, Object>> findPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String name = request.get("name");
        String email = request.get("email");
        String phoneNumber = request.get("phoneNumber");

        String maskedEmail;
        String maskedPhone;
        Long userId;

        try {
            if (phoneNumber == null && email != null) {
                String findPhoneNumber = userService.findPhoneNumberByNameAndEmail(name, email);
                if (findPhoneNumber == null) throw new RuntimeException();

                maskedEmail = maskEmail(email);
                maskedPhone = maskPhoneNumber(findPhoneNumber);
                userId = userService.getUserIdByUsername(username);
            } else if (email == null && phoneNumber != null) {
                String findEmail = userService.findEmailByNameAndPhone(name, phoneNumber);
                if (findEmail == null) throw new RuntimeException();

                maskedPhone = maskPhoneNumber(phoneNumber);
                maskedEmail = maskEmail(findEmail);
                userId = userService.getUserIdByUsername(username);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "이메일 또는 전화번호 중 하나만 입력하세요."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "일치하는 계정을 찾을 수 없습니다."));
        }

        return ResponseEntity.ok(Map.of(
                "email", maskedEmail,
                "phoneNumber", maskedPhone,
                "userId", userId
        ));
    }

    /**
     * 이메일 or 전화번호로 비밀번호 재설정 페이지 발송 컨트롤러
     */
    @PostMapping("/find-password/send")
    public ResponseEntity<Map<String, Object>> sendPassword(@RequestBody Map<String, String> response) {
        Long userId;
        String type = response.get("type"); // "email" 또는 "phone"

        if (type == null || response.get("userId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "필수 정보를 입력하세요."));
        }

        try {
            userId = Long.valueOf(response.get("userId"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId 형식이 올바르지 않습니다."));
        }

        // DB에서 회원정보 조회
        String username;
        String name;
        String email;
        String phoneNumber;

        Buyer buyer = buyerRepository.findById(userId).orElse(null);
        if (buyer != null) {
            username =  buyer.getUsername();
            name = buyer.getName();
            email = buyer.getEmail();
            phoneNumber = buyer.getPhoneNumber();
        } else {
            Seller seller = sellerRepository.findById(userId).orElse(null);
            if (seller != null) {
                username = seller.getUsername();
                name = seller.getName();
                email = seller.getEmail();
                phoneNumber = seller.getPhoneNumber();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "해당 회원을 찾을 수 없습니다."));
            }
        }

        boolean result;

        if ("email".equalsIgnoreCase(type)) {
            result = userService.sendPasswordResetLinkByEmail(username, name, email);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 이메일로 전송되었습니다."));
            }
        } else if ("phone".equalsIgnoreCase(type)) {
            result = userService.sendPasswordResetLinkByPhone(username, name, phoneNumber);
            if (result) {
                return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 링크가 휴대폰으로 전송되었습니다."));
            }
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "type은 'email' 또는 'phone' 이어야 합니다."));
        }

        return ResponseEntity.ok(Map.of("message", "비밀번호 재설정 페이지가 " + ("email".equalsIgnoreCase(type) ? "이메일" : "휴대폰") + "으로 전송되었습니다."));
    }

    /**
     * 비밀번호 재설정 페이지 컨트롤러
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        String username = redisUtil.getData("reset-token:" + token);
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "토큰이 유효하지 않거나 만료되었습니다."));
        }

        userService.resetPasswordByToken(username, newPassword); // currentPassword 검증 없이 강제 변경
        redisUtil.deleteData("reset-token:" + token);
        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails pd)) {
            result.put("authenticated", false);
            return result;
        }
        String username = pd.getUsername();
        boolean due = userService.isPasswordChangeDue(username);
        boolean snoozed = userService.isSnoozed(username);

        result.put("authenticated", true);
        // 프론트에서 이 두 값으로 팝업 노출 여부를 판단
        result.put("passwordChangeDue", due);
        result.put("passwordChangeSnoozed", snoozed);
        return result;
    }

    // 사용자가 "나중에" 클릭 시 호출
    @PostMapping("/password-change/snooze")
    public void snooze(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails pd)) {
            throw new RuntimeException("인증 필요");
        }
        // 7일(604,800초) 억제
        userService.snoozePasswordReminder(pd.getUsername(), 604800L);
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
