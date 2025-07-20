package com.shop.respawn.security.auth;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.BuyerRepository;
import com.shop.respawn.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Setter
public class PrincipalDetailsService implements UserDetailsService {

    private final BuyerRepository buyerRepository;
    private final SellerRepository sellerRepository;

    // 시큐리티 session(내부 Authentication(내부 UserDetails))
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("넘어온 유저네임: " + username);
        System.out.println("loadUserByUsername 실행");

        // 사용자 조회, 없으면 예외 발생
        Buyer buyer = buyerRepository.findByUsername(username);
        Seller seller = sellerRepository.findByUsername(username);
        if (buyer != null) {
            return new PrincipalDetails(buyer);
        } else if (seller != null) {
            return new PrincipalDetails(seller);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

    }

}
