package com.shop.respawn.security.auth;

import com.shop.respawn.domain.Admin;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import com.shop.respawn.repository.AdminRepository;
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
    private final AdminRepository adminRepository;

    // 시큐리티 session(내부 Authentication(내부 UserDetails))
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 사용자 조회, 없으면 예외 발생
        Buyer buyer = buyerRepository.findByUsername(username);
        Seller seller = sellerRepository.findByUsername(username);
        Admin admin = adminRepository.findByUsername(username);
        if (buyer != null) {
            buyer.getAccountStatus().isAccountNonExpired();
            buyerRepository.save(buyer);
            return new PrincipalDetails(buyer);
        } else if (seller != null) {
            seller.getAccountStatus().isAccountNonExpired();
            sellerRepository.save(seller);
            return new PrincipalDetails(seller);
        } else if (admin != null) {
            adminRepository.save(admin);
            return new PrincipalDetails(admin);
        } else {
            throw new UsernameNotFoundException(username + " : 사용자를 찾을 수 없습니다");
        }

    }

}
