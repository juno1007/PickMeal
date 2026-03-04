package PickMeal.PickMeal.config;

import PickMeal.PickMeal.domain.User;
import PickMeal.PickMeal.service.UserService;
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
import java.util.Map;

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
            // 2. 신규 회원: 가입 페이지로 이동
            String pureSocialId = userId.replace(registrationId + "_", "");

            // [중요 수정] 이제 제공자마다 다르게 꺼내던 복잡한 로직 대신,
            // 서비스에서 담아준 정제된 변수들을 그대로 queryParam에 넣습니다.
            String targetUrl = UriComponentsBuilder.fromUriString("/users/signup/social")
                    .queryParam("socialId", pureSocialId)
                    .queryParam("email", (email != null) ? email : "")
                    .queryParam("site", registrationId)
                    .queryParam("name", (nickname != null) ? nickname : "") // 컨트롤러가 name으로 받고 있으니 name으로 전달
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}