package com.shop.respawn.security.auth;

import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Seller;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private Buyer buyer;
    private Seller seller;
    private Map<String, Object> attributes;

    // 일반 로그인
    public PrincipalDetails(Buyer buyer) {
        this.buyer = buyer;
    }

    public PrincipalDetails(Seller seller) {
        this.seller = seller;
    }

    // OAuth 로그인
    public PrincipalDetails(Buyer buyer, Map<String, Object> attributes) {
        this.buyer = buyer;
        this.attributes = attributes;
    }

    // 해당 유저의 권한을 리턴하는 곳
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (buyer != null) {
            return List.of(new SimpleGrantedAuthority(buyer.getRole().name()));
        } else if (seller != null) {
            return List.of(new SimpleGrantedAuthority(seller.getRole().name()));
        }
        return List.of();
    }

    @Override
    public String getPassword() {
        if (buyer != null) {
            return buyer.getPassword();
        } else if (seller != null) {
            return seller.getPassword();
        }
        return null;
    }

    @Override
    public String getUsername() {
        if (buyer != null) {
            return buyer.getUsername();
        } else if (seller != null) {
            return seller.getUsername();
        }
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
