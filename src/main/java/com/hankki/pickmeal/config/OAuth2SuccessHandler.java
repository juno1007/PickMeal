package com.hankki.pickmeal.config;

import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 소셜 로그인 성공 후 처리를 담당하는 핸들러
 * 단순 로그인을 넘어 기존 회원 여부에 따라 경로를 배정해주는 '교통 정리' 역할을 함
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService; // 우리 서비스의 DB와 통신하여 유저 정보를 조회하기 위한 서비스입니다.

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        // 1. 서비스에서 이미 정제해서 넣어준 데이터들을 꺼냅니다.
        String userId = (String) oAuth2User.getAttributes().get("db_id");
        String email = (String) oAuth2User.getAttributes().get("email");       // 추가됨
        String nickname = (String) oAuth2User.getAttributes().get("nickname"); // 추가됨

        if (userId == null) userId = oAuth2User.getName();

        User findUser = userService.findById(userId);

        if (findUser != null) {
            getRedirectStrategy().sendRedirect(request, response, "/next-page");
        } else {
            String pureSocialId = userId.replace(registrationId + "_", "");

            String targetUrl = UriComponentsBuilder.fromUriString("/users/signup/social")
                    .queryParam("socialId", pureSocialId)
                    .queryParam("email", (email != null) ? email : "")
                    .queryParam("site", registrationId)
                    .queryParam("name", (nickname != null) ? nickname : "")
                    .queryParam("isSocial", true) // 추가: 가입 폼에서 소셜 유저임을 명확히 인지하도록
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}