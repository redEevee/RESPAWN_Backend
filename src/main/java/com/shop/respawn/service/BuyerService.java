package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.QBuyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.dto.BuyerDto;
import com.shop.respawn.exception.ApiException;
import com.shop.respawn.exception.ErrorCode;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shop.respawn.domain.QBuyer.buyer;

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
        String password = buyerDto.getPassword();
        String email = buyerDto.getEmail();
        String phoneNumber = buyerDto.getPhoneNumber();

        Boolean isExist = buyerRepository.existsByUsername(username);
        if (isExist) {
            throw new ApiException(ErrorCode.ALREADY_EXIST_USERNAME);
        }

        Buyer buyer = Buyer.createBuyer(name, username, encoder.encode(password), email, phoneNumber, Role.USER);

        buyerRepository.save(buyer);
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
