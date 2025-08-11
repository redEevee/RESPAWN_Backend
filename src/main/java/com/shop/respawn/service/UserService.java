package com.shop.respawn.service;

import com.nimbusds.oauth2.sdk.GeneralException;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.UserDto;
import com.shop.respawn.email.EmailService;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import com.shop.respawn.sms.SmsService;
import com.shop.respawn.util.MaskingUtil;
import com.shop.respawn.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.shop.respawn.util.MaskingUtil.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final BCryptPasswordEncoder encoder;
    private final RedisUtil redisUtil;

    /**
     * 회원가입
     */
    public void join(UserDto userDto){
        String userType = userDto.getUserType();
        String name = userDto.getName();
        String username = userDto.getUsername();
        String company = userDto.getCompany();
        Long companyNumber = userDto.getCompanyNumber();
        String password = encoder.encode(userDto.getPassword());
        System.out.println("비밀번호 인코딩:" + password);
        String email = userDto.getEmail();
        String phoneNumber = userDto.getPhoneNumber();

        if(userType.equals("buyer")){
            Buyer buyer = Buyer.createBuyer(name, username, password, email, phoneNumber, "local", Role.ROLE_USER);
            buyerRepository.save(buyer);
        } else if (userType.equals("seller")){
            Seller seller = Seller.createSeller(name, username, company, companyNumber, password, email, phoneNumber, Role.ROLE_SELLER);
            sellerRepository.save(seller);
        }
    }

    public void updatePhoneNumber(String username, String newPhoneNumber) {
        Buyer buyer = buyerRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        buyer.updatePhoneNumber(newPhoneNumber);
        // userRepository.save(user); // 트랜잭션 내 변경감지로 자동 업데이트 됩니다.
    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        Buyer buyer = buyerRepository.findByUsername(username);
        Seller seller = sellerRepository.findByUsername(username);

        if (buyer != null) {
            if (!encoder.matches(currentPassword, buyer.getPassword())) {
                return false; // 현재 비밀번호 불일치
            }
            buyer.updatePassword(encoder.encode(newPassword));
            // 변경감지(트랜잭션 내)로 save 호출 불필요
            return true;
        } else if (seller != null) {
            if (!encoder.matches(currentPassword, seller.getPassword())) {
                return false;
            }
            seller.updatePassword(encoder.encode(newPassword));
            return true;
        }
        throw new RuntimeException("사용자를 찾을 수 없습니다.");
    }

    public boolean resetPasswordByToken(String username, String newPassword) {
        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            buyer.updatePassword(encoder.encode(newPassword));
            return true;
        }

        Seller seller = sellerRepository.findByUsername(username);
        if (seller != null) {
            seller.updatePassword(encoder.encode(newPassword));
            return true;
        }

        return false; // 사용자를 찾지 못함
    }


    public Buyer getBuyerInfo(String username){
        return buyerRepository.findByUsername(username);
    }

    public Seller getSellerInfo(String username){
        return sellerRepository.findByUsername(username);
    }

    /**
     * 이름+이메일로 아이디 찾기(마스킹 버전)
     */
    public String findMaskedUsernameByNameAndEmail(String name, String email) {
        String username = findUsernameByNameAndEmail(name, email);
        return maskMiddleFourChars(username);
    }

    /**
     * 이름+전화번호로 아이디 찾기(마스킹 버전)
     */
    public String findMaskedUsernameByNameAndPhone(String name, String phoneNumber) {
        String username = findUsernameByNameAndPhone(name, phoneNumber);
        return maskMiddleFourChars(username);
    }

    /**
     * 이메일로 실제 username 찾기
     */
    private String findUsernameByNameAndEmail(String name, String email) {
        Buyer buyer = buyerRepository.findByNameAndEmail(name, email);
        if (buyer != null) return buyer.getUsername();

        Seller seller = sellerRepository.findByNameAndEmail(name, email);
        if (seller != null) return seller.getUsername();

        throw new RuntimeException("해당 이름과 이메일로 가입된 사용자가 없습니다.");
    }

    public String getRealUsernameByNameAndEmail(String name, String email) {
        return findUsernameByNameAndEmail(name, email);
    }

    /**
     * 전화번호로 실제 username 찾기
     */
    private String findUsernameByNameAndPhone(String name, String phone) {
        Buyer buyer = buyerRepository.findByNameAndPhoneNumber(name, phone);
        if (buyer != null) return buyer.getUsername();

        Seller seller = sellerRepository.findByNameAndPhoneNumber(name, phone);
        if (seller != null) return seller.getUsername();

        throw new RuntimeException("해당 이름과 전화번호로 가입된 사용자가 없습니다.");
    }

    public String getRealUsernameByNameAndPhone(String name, String phone) {
        return findUsernameByNameAndPhone(name, phone);
    }

    /**
     * 2단계 - 이메일로 실제 아이디 전송
     */
    public void sendRealUsernameByEmail(String name, String email) {
        String realUsername = findUsernameByNameAndEmail(name, email);
        String message = "회원님의 아이디는 [" + realUsername + "] 입니다.";
        emailService.sendEmailUsernameAsync(email, message);
    }

    /**
     * 2단계 - 휴대폰으로 실제 아이디 전송
     */
    public void sendRealUsernameByPhone(String name, String phoneNumber) {
        String realUsername = findUsernameByNameAndPhone(name, phoneNumber);
        String username = "회원님의 아이디는 [" + realUsername + "] 입니다.";
        smsService.sendUsernameMessage(phoneNumber, username);
    }

    public String findPhoneNumberByNameAndEmail(String name, String email) {
        Buyer buyer = buyerRepository.findByNameAndEmail(name, email);
        if (buyer != null) {
            return buyer.getPhoneNumber();
        }
        Seller seller = sellerRepository.findByNameAndEmail(name, email);
        if (seller != null) {
            return seller.getPhoneNumber();
        }
        throw new RuntimeException("해당 이름과 이메일로 가입된 사용자가 없습니다.");
    }

    public String findEmailByNameAndPhone(String name, String phoneNumber) {
        Buyer buyer = buyerRepository.findByNameAndPhoneNumber(name, phoneNumber);
        if (buyer != null) {
            return buyer.getEmail();
        }

        Seller seller = sellerRepository.findByNameAndPhoneNumber(name, phoneNumber);
        if (seller != null) {
            return seller.getEmail();
        }

        throw new RuntimeException("해당 이름과 전화번호로 가입된 사용자가 없습니다.");
    }

    public boolean sendPasswordResetLinkByEmail(String username, String name, String email) {
        Buyer buyer = buyerRepository.findByUsernameAndNameAndEmail(username, name, email);
        Seller seller = sellerRepository.findByUsernameAndNameAndEmail(username, name, email);

        if (buyer == null && seller == null) {
            return false; // 일치하는 유저 없음
        }

        // 임시 토큰 생성 (10분 만료)
        String token = UUID.randomUUID().toString();
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        // Redis나 DB에 토큰 저장
        redisUtil.setDataExpire("reset-token:" + token, username, 10 * 60L);

        // 이메일 발송
        emailService.sendPasswordResetLink(email, resetLink);
        return true;
    }

    public boolean sendPasswordResetLinkByPhone(String username, String name, String phoneNumber) {
        Buyer buyer = buyerRepository.findByUsernameAndNameAndPhoneNumber(username, name, phoneNumber);
        Seller seller = sellerRepository.findByUsernameAndNameAndPhoneNumber(username, name, phoneNumber);

        if (buyer == null && seller == null) {
            return false;
        }

        String token = UUID.randomUUID().toString();
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        redisUtil.setDataExpire("reset-token:" + token, username, 30 * 60L);

        smsService.sendPasswordResetLink(phoneNumber, resetLink);
        return true;
    }

    public void storeUsernameToken(String token, String username) {
        // 예: 10분(600초) 만료시간 설정
        redisUtil.setDataExpire("find-id-token:" + token, username, 600L);
    }

    public String getUsernameByToken(String token) {
        return redisUtil.getData("find-id-token:" + token);
    }

    public void deleteUsernameToken(String token) {
        redisUtil.deleteData("find-id-token:" + token);
    }

    public boolean verifyUsernameNameEmail(String username, String name, String email) {
        // 이름과 이메일이 실제 유저 정보와 일치하는지 검증
        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            return buyer.getName().equals(name) && buyer.getEmail().equals(email);
        }
        Seller seller = sellerRepository.findByUsername(username);
        if (seller != null) {
            return seller.getName().equals(name) && seller.getEmail().equals(email);
        }
        return false;
    }

    public boolean verifyUsernameNamePhone(String username, String name, String phoneNumber) {
        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            return buyer.getName().equals(name) && buyer.getPhoneNumber().equals(phoneNumber);
        }
        Seller seller = sellerRepository.findByUsername(username);
        if (seller != null) {
            return seller.getName().equals(name) && seller.getPhoneNumber().equals(phoneNumber);
        }
        return false;
    }

    public Long getUserIdByUsername(String username) {
        Buyer buyer = buyerRepository.findByUsername(username);
        if (buyer != null) {
            return buyer.getId();
        }
        Seller seller = sellerRepository.findByUsername(username);
        if (seller != null) {
            return seller.getId();
        }
        throw new RuntimeException("해당 아이디의 사용자를 찾을 수 없습니다.");
    }

    /**
     * 아이디 중복확인
     */
    public boolean checkUsernameDuplicate(String username) {
        return buyerRepository.existsByUsername(username) || sellerRepository.existsByUsername(username);
    }

    /**
     * 전화번호 중복확인
     */
    public boolean checkPhoneNumberDuplicate(String phoneNumber) {
        return buyerRepository.existsByPhoneNumber(phoneNumber) || sellerRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 이메일 중복확인
     */
    public boolean checkEmailDuplicate(String email) {
        return buyerRepository.existsByEmail(email) || sellerRepository.existsByEmail(email);
    }

    /**
     * 비밀번호 확인
     */
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
