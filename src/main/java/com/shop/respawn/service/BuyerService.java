package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class BuyerService {

    private final BuyerRepository buyerRepository;

    /**
     * 회원가입
     */
    @Transactional
    public void join(Buyer buyer){
        buyerRepository.save(buyer);
    }

    private void validateDuplicateBuyer(Buyer buyer) {
        List<Buyer> findByUsername = buyerRepository.findByUsername(buyer.getUsername());
        if (!findByUsername.isEmpty()) {
            throw new IllegalStateException("이미 사용 중인 아이디입니다.");
        }
    }

    /**
     * 아이디 중복확인
     */
    public boolean checkUsernameDuplicate(String username) {
        return buyerRepository.existsByUsername(username);
    }
}
