package com.shop.respawn.auth;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final BuyerRepository buyerRepository;

    // 시큐리티 session(내부 Authentication(내부 UserDetails))
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Buyer buyer = buyerRepository.findAuthByUsername(username);

        if (buyer == null) {
            System.out.println("Username not found");
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        System.out.println("Username found: " + username);
        return new PrincipalDetails(buyer);
    }
}
