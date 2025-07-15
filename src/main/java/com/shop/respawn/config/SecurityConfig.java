package com.shop.respawn.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable) //csrf 검증 비활성화
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));// ⭐️ CORS 설정 추가

        /*
            requestMatchers에 특정 url을 입력하면 해당 url에 대한 접근 권한을 설정
            permitAll(): 인증받지 않은 유저(로그인 하지 않은 유저)에게도 접근을 허용
            hasRole(): 특정 권한을 갖고 있는 유저에게만 접근을 허용
            hasAnyRole(): 인수에 전달한 여러 권한 중 하나라도 가진 유저에게 접근을 허용
         */
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                        .anyRequest().permitAll()
                );

        http //일반 로그인
                .formLogin(auth -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/loginProc")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/loginOk")
                        .permitAll()
                );

        http //로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")                  // 로그아웃 요청 URL (기본값: "/logout")
                        .logoutSuccessUrl("/logoutOk")          // 로그아웃 성공 후 리다이렉트 URL (기본값: "/login?logout")
                        .invalidateHttpSession(true)           // 세션 무효화 (기본값: true)
                        .deleteCookies("JSESSIONID")           // 쿠키 삭제
                );

        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                );

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)  // 필요한 경우만 세션 생성
                        .sessionFixation().changeSessionId()                      // 세션 고정 공격 방지
                        .maximumSessions(1)                                       // 최대 동시 로그인 1개
                        .maxSessionsPreventsLogin(true)                       // 기존 세션 유지, 새 로그인 차단
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