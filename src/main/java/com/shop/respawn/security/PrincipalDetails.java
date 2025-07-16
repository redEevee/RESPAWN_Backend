package com.shop.respawn.security;

import com.shop.respawn.domain.Buyer;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private Buyer buyer;
    private Map<String, Object> attributes;

    // 일반 로그인
    public PrincipalDetails(Buyer buyer) {
        this.buyer = buyer;
    }
    // OAuth 로그인
    public PrincipalDetails(Buyer buyer, Map<String, Object> attributes) {
        this.buyer = buyer;
        this.attributes = attributes;
    }

    // 해당 유저의 권한을 리턴하는 곳
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(buyer.getRole().name()));
    }

    @Override
    public String getPassword() {
        return buyer.getPassword();
    }

    @Override
    public String getUsername() {
        return buyer.getUsername();
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
