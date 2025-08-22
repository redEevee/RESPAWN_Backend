package com.shop.respawn.controller;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.ApiMessage;
import com.shop.respawn.dto.findInfo.findIdRequest;
import com.shop.respawn.dto.findInfo.findIdResponse;
import com.shop.respawn.dto.user.*;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.security.auth.PrincipalDetails;
import com.shop.respawn.service.UserService;
import com.shop.respawn.util.RedisUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.shop.respawn.exception.status_code.ErrorStatus.*;
import static com.shop.respawn.exception.status_code.SuccessStatus.*;
import static com.shop.respawn.util.MaskingUtil.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final RedisUtil redisUtil;

    /**
     * 회원 가입
     */
    @PostMapping("/join")
    public ResponseEntity<Void> join(@RequestBody UserDto userDto) {
        userService.join(userDto);
        return ResponseEntity.ok().build();
    }

    /**
     * session 동기화/초기화
     */
    @GetMapping("/bring-me")
    public ResponseEntity<LoginOkResponse> bringMe(Authentication authentication) {
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        LoginOkResponse data = userService.getUserData(authorities, username);
        if (data == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(data);
    }

    /**
     * 로그인 완료 처리
     */
    @GetMapping("/loginOk")
    public ResponseEntity<LoginOkResponse> loginOk() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();
        return ResponseEntity.ok(userService.getUserData(authorities, username));
    }

    /**
     * 로그아웃
     */
    @GetMapping("/logoutOk")
    public ResponseEntity<?> logoutOk() {
        return ResponseEntity.ok().build();
    }

    /**
     * 관리자 정보 조회
     */
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
    public ResponseEntity<?> checkPassword(Authentication authentication,
                                                 @Valid @RequestBody PasswordRequest request) {
        if(userService.isMatchPassword(authentication.getName(), request.getPassword())) {
            return ResponseEntity.ok(ApiMessage.of(_PASSWORD_CHECKED));
        } else {
            return ResponseEntity.badRequest().body(ApiMessage.of(_PASSWORD_MISMATCH));
        }
    }

    /**
     * 전화번호 수정 엔드포인트
     */
    @PutMapping("/myPage/setPhoneNumber")
    public ResponseEntity<?> updatePhoneNumber(Authentication authentication,
                                                  @Valid @RequestBody PhoneNumberRequest request) {
        userService.updatePhoneNumber(authentication.getName(), request.getPhoneNumber());
        return ResponseEntity.ok(
                ApiMessage.of("PHONE-NUMBER_CHANGED", "전화번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 비밀번호 변경 엔드포인트
     */
    @PutMapping("/myPage/setPassword")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        String username = authentication.getName();
        if (userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword())) {
            return ResponseEntity.ok(
                    ApiMessage.of("PASSWORD_CHANGED", "비밀번호가 성공적으로 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(
                    ApiMessage.of("PASSWORD_MISMATCH", "현재 비밀번호가 일치하지 않습니다."));
        }
    }

    /**
     * 이름 + 이메일 or 전화번호로 마스킹된 아이디 찾기
     */
    @PostMapping("/find-id")
    public ResponseEntity<findIdResponse> findId(@RequestBody findIdRequest findIdRequest) {
        try {
            findIdResponse response = userService.findId(findIdRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new findIdResponse(e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new findIdResponse(e.getMessage()));
        }
    }

    /**
     * 이메일 or 전화번호로 실제 아이디 전송 컨트롤러
     */
    @PostMapping("/find-id/send")
    public ResponseEntity<?> sendId(@RequestBody findIdRequest findIdRequest) {
        try {
            String message = userService.processSendId(findIdRequest);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
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
