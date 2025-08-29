package com.shop.respawn.security.oauth.provider;

import java.util.Map;

/**
 * @param attributes getAttributes()
 */
public record KakaoUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getEmail() {
        Object object = attributes.get("kakao_account");
        if (object instanceof Map) {
            Map<String, Object> accountMap = (Map<String, Object>) object;
            return (String) accountMap.get("email");
        }
        return null;
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getPhoneNumber() {
        return (String) attributes.get("phone_number");
    }
}