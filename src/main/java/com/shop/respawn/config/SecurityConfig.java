package com.shop.respawn.config;

import com.shop.respawn.config.oauth.PrincipalOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터가 스프링 필터체인에 등록됨
public class SecurityConfig {

    // 해당 메서드의 리턴되는 오브젝트를 IOC로 등록해준다.
    @Bean
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, PrincipalOauth2UserService principalOauth2UserService) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(authorize -> {
            authorize
                    .requestMatchers("/mainBanner/**").authenticated()
                    .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                    .anyRequest().permitAll();
        });

        http.formLogin(form -> {
            form
                    .loginPage("/loginForm") // 또는 존재하지 않는 dummy URL
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("http://localhost:3000") // 로그인 성공 후 프론트 주소로 이동
                    .permitAll();
        });

        http.oauth2Login(form -> {
            form
                    .loginPage("/loginForm")
                    .userInfoEndpoint(userInfoEndpointConfig -> {
                        userInfoEndpointConfig.userService(principalOauth2UserService);
                    });
        });

        http
                .logout(logout -> logout
                        .logoutUrl("/logout")                  // 로그아웃 요청 URL (기본값: "/logout")
                        .logoutSuccessUrl("/")                 // 로그아웃 성공 후 리다이렉트 URL (기본값: "/login?logout")
                        .invalidateHttpSession(true)           // 세션 무효화 (기본값: true)
                        .deleteCookies("JSESSIONID")           // 쿠키 삭제
                );

        return http.build();
    }
}