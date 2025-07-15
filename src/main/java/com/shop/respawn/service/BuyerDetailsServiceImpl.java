package com.shop.respawn.service;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Setter
@RequiredArgsConstructor
@Service
public class BuyerDetailsServiceImpl implements UserDetailsService {

    private final BuyerRepository buyerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("넘어온 이메일: " + username);
        System.out.println("loadUserByUsername 실행");

        // 사용자 조회, 없으면 예외 발생
        Buyer buyer = buyerRepository.findByUsername(username);
        if(buyer == null){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 사용자가 있다면 UserDetails 객체 생성
        return new User(
                buyer.getUsername(),
                buyer.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority(buyer.getRole().name()))
        );
    }
}
