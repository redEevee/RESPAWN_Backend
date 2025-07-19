package com.shop.respawn.email;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    // 인증번호 전송
    @GetMapping("/auth")
    public EmailAuthResponseDto sendAuthCode(@RequestParam String address) {
        emailService.sendEmailAsync(address);

        return new EmailAuthResponseDto(true, "인증번호가 메일로 전송 중입니다.");
    }

    // 인증번호 검증
    @PostMapping("/auth")
    public EmailAuthResponseDto checkAuthCode(@RequestParam String address, @RequestParam String authCode) {
        return emailService.validateAuthCode(address, authCode);
    }
}
