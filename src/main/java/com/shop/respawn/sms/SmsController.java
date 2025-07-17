package com.shop.respawn.sms;

import java.time.LocalDateTime;

import com.nimbusds.oauth2.sdk.GeneralException;
import com.shop.respawn.api.api_payload.CommonResponse;
import com.shop.respawn.api.api_payload.status_code.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.shop.respawn.sms.dto.SmsRequest.*;

@RestController
@RequiredArgsConstructor
public class SmsController {
    private final SmsService smsService;

    @PostMapping("/verify-phone-number")
    public CommonResponse<Void>
    getPhoneNumberForVerification(@RequestBody PhoneNumberForVerificationRequest request) {
        LocalDateTime sentAt = LocalDateTime.now();
        smsService.sendVerificationMessage(request.getPhoneNumber(), sentAt);
        return CommonResponse.of(SuccessStatus._ACCEPTED, null);
    }

    @PostMapping("/phone-number/verification-code")
    public CommonResponse<String> verificationByCode(@RequestBody VerificationCodeRequest request) throws GeneralException {
        LocalDateTime verifiedAt = LocalDateTime.now();
        smsService.verifyCode(request.getCode(), verifiedAt);
        return CommonResponse.ok("정상 인증 되었습니다.");
    }
}
