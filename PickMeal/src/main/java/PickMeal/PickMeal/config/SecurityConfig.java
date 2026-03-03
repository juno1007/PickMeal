package PickMeal.PickMeal.config;

import PickMeal.PickMeal.service.CustomOAuth2UserService;
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
                        "/board/write", "/file/upload", "/api/wishlist/**", "/api/restaurant/**", "/api/review/**"
                ))
                .authorizeHttpRequests(authorize -> authorize
                        // 1. 구체적인 인증 필요 경로
                        .requestMatchers("/board/write/**", "/board/remove/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/users/mypage", "/users/edit","/board/edit/**" ).authenticated()

                        // 2. 게시판 목록 및 상세는 누구나 접근 가능 (순서 중요!)
                        .requestMatchers("/board/list", "/board/detail/**", "/board/meal-spotter").permitAll()

                        // 3. 나머지 공통 허용
                        .requestMatchers("/", "/next-page", "/hotplace",
                                "/users/signup", "/users/signup/social", "/users/login",
                                "/users/check-id", "/users/check-nickname", "/users/find-id",
                                "/users/forgot-pw", "/users/find-password/**", "/users/reset-password/**",
                                "/mail/**", "/oauth2/**", "/css/**", "/js/**", "/images/**", "/*.json",
                                "/roulette", "/twentyQuestions/**", "/twenty-questions/**", "/capsule", "/game/**", "/worldcup/**",
                                "/api/**", "/draw", "/meal-spotter", "/hotplace/**").permitAll()

                        .anyRequest().authenticated()
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