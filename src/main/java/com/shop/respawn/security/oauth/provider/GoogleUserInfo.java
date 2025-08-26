package com.shop.respawn.security.oauth.provider;

import java.util.Map;

/**
 * @param attributes getAttributes()
 */
public record GoogleUserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
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
        return (String) attributes.get("phone_number");
    }
}