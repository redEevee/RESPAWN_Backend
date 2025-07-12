package com.shop.respawn.config.oauth;

import com.shop.respawn.config.oauth.provider.GoogleUserInfo;
import com.shop.respawn.config.oauth.provider.KakaoUserInfo;
import com.shop.respawn.config.oauth.provider.NaverUserInfo;
import com.shop.respawn.config.oauth.provider.OAuth2UserInfo;
import com.shop.respawn.domain.Buyer;
import com.shop.respawn.domain.Role;
import com.shop.respawn.auth.PrincipalDetails;
import com.shop.respawn.repository.BuyerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private BuyerRepository buyerRepository;

    // 구글로부터 받은 userRequest 데이터에 대한 후처리되는 함수
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration = " + userRequest.getClientRegistration()); // registrationId로 어떤 OAuth로 로그인 했는지 확인 가능
        System.out.println("getAccessToken = " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 구글로그인 버튼 클릭 -> 구글로그인창 -> 로그인을 완료 -> code를 리턴(OAuth2-Client 라이브러리) -> AccessToken 요청
        // userRequest 정보 -> 회원 프로필 받아야함(loadUser함수 호출) -> 구글로부터 회원프로필 받아준다.
        System.out.println("getAttributes = " + oAuth2User.getAttributes());

        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            System.out.println("카카오 로그인 요청");
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            System.out.println("소셜 로그인은 구글, 네이버, 카카오만 지원합니다.");
        }

        assert oAuth2UserInfo != null;
        String provider = oAuth2UserInfo.getProvider(); // google
        String providerId = oAuth2UserInfo.getProviderId();
        String name = oAuth2UserInfo.getName();
        String username = provider + "_" + providerId; // google_10021320120
        String password = bCryptPasswordEncoder.encode("social_login");
        String email = oAuth2UserInfo.getEmail();
        String phoneNumber = oAuth2UserInfo.getPhoneNumber();
        String role = Role.ROLE_USER.toString();

        Buyer userEntity = buyerRepository.findAuthByUsername(username);

        if (userEntity == null) {
            System.out.println("소셜 로그인이 최초입니다.");
            userEntity = Buyer.builder()
                    .name(name)
                    .username(username)
                    .password(password)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .role(Role.valueOf(role))
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            buyerRepository.save(userEntity);
        } else {
            System.out.println("소셜 로그인을 이미 한적이 있습니다. 자동회원가입이 되어 있습니다.");
        }

        // 회원 가입을 강제로 진행해볼 예정
        return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
    }
}