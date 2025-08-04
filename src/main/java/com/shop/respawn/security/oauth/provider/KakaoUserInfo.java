package com.shop.respawn.security.oauth.provider;

import java.util.LinkedHashMap;
import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo{

    private final Map<String, Object> attributes; // getAttributes()
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

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
}