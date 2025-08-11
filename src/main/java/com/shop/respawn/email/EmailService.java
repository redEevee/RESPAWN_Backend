package com.shop.respawn.email;

import com.shop.respawn.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String senderEmail;

    private final JavaMailSender mailSender;
    private final RedisUtil redisUtil;

    @Async
    public void sendEmailAsync(String toEmail) {
        if (redisUtil.existData(toEmail)) {
            redisUtil.deleteData(toEmail);
        }

        try {
            MimeMessage emailForm = createEmailForm(toEmail);
            mailSender.send(emailForm);
            System.out.println("이메일이 전송 되었습니다.");
        } catch (MessagingException | MailSendException e) {
            log.error("메일 전송 중 오류가 발생하였습니다. 다시 시도해주세요.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createEmailForm(String email) throws MessagingException, UnsupportedEncodingException {

        String authCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(senderEmail, "Respawn"));
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("인증코드 안내");
        message.setText(setContext(authCode), "utf-8", "html");

        redisUtil.setDataExpire(email, authCode, 10 * 60L); // 10분
        return message;
    }

    private String setContext(String authCode) {
        String body = "";
        body += "<div style='width:100%;max-width:400px;margin:0 auto;font-family:sans-serif;border:1px solid #e5e5e5;padding:32px 16px;background:#fff;'>"
                + "  <div style='text-align:center;margin-bottom:24px;'>"
                +      "<h1 style='font-size:36px;color:#3465db;margin:0;'>RESPAWN</h1>"
                +  "</div>"
                +  "<h2 style='color:#222;text-align:center;'>이메일 인증 안내</h2>"
                +  "<p style='font-size:16px;color:#444;text-align:center;margin-bottom:24px;'>RESPAWN 서비스를 이용해주셔서 감사합니다.<br>"
                +  "아래 인증번호를 입력하여 이메일 인증을 완료해 주세요.</p>"
                +  "<div style='font-size:32px;color:#3465db;font-weight:bold;background:#f2f4fa;border-radius:8px;padding:20px 0;margin:0 auto 24px;width:220px;text-align:center;'>"
                +      authCode
                +  "</div>"
                +  "<p style='font-size:14px;color:#888;text-align:center;'>이 인증번호의 유효기간은 10분입니다.</p>"
                +  "<p style='font-size:13px;color:#aaa;text-align:center;margin-top:20px;'>본 메일은 발신 전용입니다. 문의는 고객센터를 이용해 주세요.</p>"
                + "</div>";
        return body;
    }

    public EmailAuthResponseDto validateAuthCode(String email, String authCode) {
        String findAuthCode = redisUtil.getData(email);
        if (findAuthCode == null) {
            return new EmailAuthResponseDto(false, "인증번호가 만료되었습니다. 다시 시도해주세요.");
        }

        if (findAuthCode.equals(authCode)) {
            return new EmailAuthResponseDto(true, "인증 성공에 성공했습니다.");

        } else {
            return new EmailAuthResponseDto(false, "인증번호가 일치하지 않습니다.");
        }
    }

    @Async
    public void sendEmailUsernameAsync(String toEmail, String username) {
        try {
            MimeMessage emailForm = createEmailUsernameForm(toEmail, username);
            mailSender.send(emailForm);
            System.out.println("이메일이 전송 되었습니다.");
        } catch (MessagingException | MailSendException e) {
            log.error("메일 전송 중 오류가 발생하였습니다. 다시 시도해주세요.", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private MimeMessage createEmailUsernameForm(String email, String username) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(senderEmail, "Respawn"));
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("RESPAWN, 요청하신 아이디 입니다.");
        message.setText(setContextUsername(username), "utf-8", "html");

        return message;
    }

    private String setContextUsername(String username) {
        String body = "";
        body += "<div style='width:100%;max-width:800px;margin:0 auto;font-family:sans-serif;border:1px solid #e5e5e5;padding:32px 16px;background:#fff;'>"
                + "  <div style='text-align:center;margin-bottom:24px;'>"
                +      "<h1 style='font-size:36px;color:#3465db;margin:0;'>RESPAWN</h1>"
                +  "</div>"
                +  "<h2 style='color:#222;text-align:center;'>고객님의 회원아이디를 안내드립니다.</h2>"
                +  "<p style='font-size:16px;color:#444;text-align:center;margin-bottom:24px;'>고객님 안녕하세요.<br>"
                +  "고객님께서 요청하신 아이디를 안내드립니다.</p>"
                +  "<div style='font-size:28px;color:#3465db;font-weight:bold;background:#f2f4fa;border-radius:8px;padding:20px 0;margin:0 auto 24px;width:600px;text-align:center;'>"
                +      username
                +  "</div>"
                +  "<p style='font-size:14px;color:#888;text-align:center;'>RESPAWN에 접속하여 로그인 및 비밀번호 찾기를 계속해 주세요.</p>"
                +  "<p style='font-size:13px;color:#aaa;text-align:center;margin-top:20px;'>본 메일은 발신 전용입니다. 문의는 고객센터를 이용해 주세요.</p>"
                + "</div>";
        return body;
    }

    @Async
    public void sendPasswordResetLink(String toEmail, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, toEmail);
            message.setSubject("[RESPAWN] 비밀번호 재설정 안내");
            message.setText("<p>아래 링크를 클릭하여 비밀번호를 재설정하세요.</p>"
                    + "<a href='" + resetLink + "'>비밀번호 재설정</a>", "utf-8", "html");
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("비밀번호 재설정 메일 전송 실패", e);
        }
    }

}
