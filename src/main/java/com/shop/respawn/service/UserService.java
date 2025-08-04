package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.dto.UserDto;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;
    private final BCryptPasswordEncoder encoder;

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

    public Seller getSellerInfo(String username){
        return sellerRepository.findByUsername(username);
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
