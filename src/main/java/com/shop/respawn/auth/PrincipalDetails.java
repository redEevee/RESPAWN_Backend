package com.shop.respawn.auth;

import com.shop.respawn.domain.Buyer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final Buyer buyer; // 콤포지션
    private Map<String, Object> attributes;

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
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return buyer.getRole().name();
            }
        });
        return collection;
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

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
