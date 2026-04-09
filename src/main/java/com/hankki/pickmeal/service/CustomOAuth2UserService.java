package com.hankki.pickmeal.service;

import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service // 소셜 로그인 데이터 처리 서비스
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> originAttributes = oAuth2User.getAttributes();

        String rawId = "";
        String email = "";
        String nickname = "";

        // 1. 제공자별 데이터 추출 (ID, Email, Nickname)
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) originAttributes.get("response");
            rawId = String.valueOf(response.get("id"));
            email = String.valueOf(response.get("email"));
            nickname = String.valueOf(response.get("nickname"));
        } else if ("kakao".equals(registrationId)) {
            rawId = String.valueOf(originAttributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) originAttributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = String.valueOf(kakaoAccount.get("email"));
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = String.valueOf(profile.get("nickname"));
                }
            }
        } else if ("google".equals(registrationId)) {
            rawId = String.valueOf(originAttributes.get("sub"));
            email = String.valueOf(originAttributes.get("email"));
            nickname = String.valueOf(originAttributes.get("name")); // 구글은 보통 name을 사용
        }

        // 2. ID 세척 및 접두사 부여 로직 (기존 로직 유지)
        String cleanId = rawId.replace(registrationId + "_", "").replace(registrationId, "");
        String finalId = registrationId + "_" + cleanId;

        User user = userMapper.findById(finalId);
        if (user != null && "SUSPENDED".equals(user.getStatus())) {
            String dateStr = "영구";
            if (user.getSuspensionEndDate() != null) {
                // 날짜 포맷팅 (UserService와 동일하게)
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");
                dateStr = user.getSuspensionEndDate().format(formatter);
            }

            // 에러 메시지에 날짜 정보를 담아서 던집니다.
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("suspended_user"),
                    "운영원칙 위반으로 [" + dateStr + "]까지 이용이 정지되었습니다."
            );
        }

        // 4. 권한 부여 로직 분리 (중요!)
        // DB에서 조회한 user 객체가 null이면 아직 회원가입 전이므로 GUEST 권한을 부여합니다.
        String role = (user != null) ? "ROLE_USER" : "ROLE_GUEST";

        System.out.println(">>> [권한 부여 체크] ID: " + finalId + " / ROLE: " + role);

        // 5. 세션 설정을 위한 Map 구성
        Map<String, Object> customMap = new HashMap<>(originAttributes);
        customMap.put("db_id", finalId);
        customMap.put("email", email);
        customMap.put("nickname", nickname);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(role)), // 동적 권한 주입
                customMap,
                "db_id"
        );
    }
}