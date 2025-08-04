package com.shop.respawn.sms;

import com.nimbusds.oauth2.sdk.GeneralException;
import com.shop.respawn.sms.api_payload.status_code.ErrorStatus;
import com.shop.respawn.sms.Verification.VerificationCode;
import com.shop.respawn.sms.Verification.VerificationCodeGenerator;
import com.shop.respawn.sms.Verification.VerificationCodeRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${spring.sms.api-key}")
    private String apiKey;
    @Value("${spring.sms.api-secret}")
    private String apiSecret;
    @Value("${spring.sms.provider}")
    private String smsProvider;
    @Value("${spring.sms.sender}")
    private String smsSender;

    private DefaultMessageService messageService;

    private final VerificationCodeRepository verificationCodeRepository;

    /**
     * coolSms 키값 세팅
     */
    @PostConstruct
    public void init(){
        messageService = NurigoApp.INSTANCE.initialize(
                apiKey,
                apiSecret,
                smsProvider
        );
    }

    public void sendVerificationMessage(String to, LocalDateTime sentAt){
        Message message = new Message(); //메시지 객체 생성
        message.setFrom(smsSender); //메시지 보내는 사람 전화번호 등록
        message.setTo(to); //메시지 내용 등록

        VerificationCode verificationCode = VerificationCodeGenerator
                .generateVerificationCode(sentAt);
        verificationCodeRepository.save(verificationCode);

        String text = verificationCode.generateCodeMessage();
        message.setText(text);

        messageService.sendOne(new SingleMessageSendingRequest(message));
    }

    public void verifyCode(String code, LocalDateTime verifiedAt) throws GeneralException {
        VerificationCode verificationCode = verificationCodeRepository.findByCode(code)
                .orElseThrow(() -> new GeneralException(String.valueOf(ErrorStatus._VERIFICATION_CODE_NOT_FOUND)));

        if(verificationCode.isExpired(verifiedAt)){
            throw new GeneralException(String.valueOf(ErrorStatus._VERIFICATION_CODE_EXPIRED));
        }

        verificationCodeRepository.remove(verificationCode);
    }
}
