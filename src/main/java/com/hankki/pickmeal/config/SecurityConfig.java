package com.hankki.pickmeal.config;

import com.hankki.pickmeal.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFailureHandler loginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, OAuth2SuccessHandler oauth2SuccessHandler) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/mail/**", "/users/**", "/worldcup/win/**", "/hotplace/**", "/board/remove/**",
                        "/board/write", "/file/upload", "/api/wishlist/**", "/api/restaurant/**",
                        "/api/review/**", "/api/reviews/**", "/api/**"
                ))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. [통합 및 최우선] 내부 포워딩, 에러 페이지, 정적 리소스를 모두 허용
                        // 이 설정을 통해 WebSecurityCustomizer 없이도 안전하게 접근을 허용합니다.
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/*.json", "/error", "/error/**").permitAll()

                        // 2. [밀스팟 해결] 맛집 탐지기 관련 모든 경로
                        .requestMatchers("/meal-spotter", "/board/meal-spotter", "/hotplace/**", "/api/**").permitAll()

                        // 3. 인증 필요 경로
                        .requestMatchers("/board/write/**", "/board/remove/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/users/mypage", "/users/edit", "/board/edit/**").authenticated()

                        // 4. 나머지 모든 공개 경로
                        .requestMatchers("/", "/next-page", "/hotplace",
                                "/users/signup", "/users/signup/social", "/users/login",
                                "/users/check-id", "/users/check-nickname", "/users/find-id",
                                "/users/forgot-pw", "/users/find-password/**", "/users/reset-password/**",
                                "/mail/**", "/oauth2/**", "/board/list", "/board/detail/**",
                                "/roulette", "/twentyQuestions/**", "/twenty-questions/**", "/capsule", "/game/**", "/worldcup/**",
                                "/draw").permitAll()

                        .anyRequest().authenticated()
                )
                // 인증 예외 핸들링 (404 등을 로그인창 대신 에러페이지로 유도)
                .exceptionHandling(exception -> exception
                        // 1. 로그인 했으나 권한이 없는 경우 (403 전용 페이지로 이동)
                        .accessDeniedPage("/error?status=403")

                        // 2. 로그인 안 한 사용자가 보호된 페이지 접근 시 (기존 유지 혹은 로그인 페이지 유도)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/users/login");
                        })
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login")
                        .loginProcessingUrl("/users/login")
                        .usernameParameter("id")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/users/login")
                        .successHandler(oauth2SuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .failureHandler(loginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/next-page")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

}