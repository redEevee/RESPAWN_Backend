package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.BuyerDto;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final BCryptPasswordEncoder encoder;

    /**
     * 회원가입
     */
    public void join(BuyerDto buyerDto){
        String name = buyerDto.getName();
        String username = buyerDto.getUsername();
        String password = encoder.encode(buyerDto.getPassword());
        System.out.println("비밀번호 인코딩:" + password);
        String email = buyerDto.getEmail();
        String phoneNumber = buyerDto.getPhoneNumber();

        Buyer buyer = Buyer.createBuyer(name, username, password, email, phoneNumber, Role.ROLE_USER);

        buyerRepository.save(buyer);
    }

    @Transactional
    public void updatePhoneNumber(String username, String newPhoneNumber) {
        Buyer buyer = buyerRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        buyer.updatePhoneNumber(newPhoneNumber);
        // userRepository.save(user); // 트랜잭션 내 변경감지로 자동 업데이트 됩니다.
    }

    public Buyer getBuyerInfo(String username){
        return buyerRepository.findByUsername(username);
    }

    /**
     * 아이디 중복확인
     */
    public boolean checkUsernameDuplicate(String username) {
        return buyerRepository.existsByUsername(username);
    }

    /**
     * 전화번호 중복확인
     */
    public boolean checkPhoneNumberDuplicate(String phoneNumber) {
        return buyerRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 이메일 중복확인
     */
    public boolean checkEmailDuplicate(String email) {
        return buyerRepository.existsByEmail(email);
    }
}
