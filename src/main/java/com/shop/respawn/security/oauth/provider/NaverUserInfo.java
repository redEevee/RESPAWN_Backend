package com.shop.respawn.security.oauth.provider;

import java.util.Map;

/**
 * @param attributes getAttributes()
 */
public record NaverUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getPhoneNumber() {
        return (String) attributes.get("phone_Number");
    }
}
