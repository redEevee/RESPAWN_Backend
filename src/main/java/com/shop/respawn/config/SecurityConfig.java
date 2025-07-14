package com.shop.respawn.config;

import com.shop.respawn.config.oauth.PrincipalOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ⭐️ CORS 설정 추가
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/login", "/join").permitAll()
                    .requestMatchers("/mainBanner/**").authenticated()
                    .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                    .anyRequest().permitAll());

        http
                .sessionManagement(sessionManagement -> sessionManagement
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                );

        http.formLogin(form -> {
            form
                    .loginPage("/dummy") // 또는 존재하지 않는 dummy URL
                    .loginProcessingUrl("/login")
                    .permitAll();
        });

        http.oauth2Login(form -> {
            form
                    .loginPage("/loginForm")
                    .userInfoEndpoint(userInfoEndpointConfig -> {
                        userInfoEndpointConfig.userService(principalOauth2UserService);
                    })
                    .defaultSuccessUrl("http://localhost:3000", true);
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // ★ withCredentials: true와 같이 사용하려면 꼭 true로 설정
        config.setAllowedOrigins(List.of("http://localhost:3000")); // * 사용하지 말고 정확하게 지정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}