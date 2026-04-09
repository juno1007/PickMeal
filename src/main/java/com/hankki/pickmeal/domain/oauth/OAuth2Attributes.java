package com.hankki.pickmeal.domain.oauth;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuth2Attributes { // 소셜별로 제각각인 응답 데이터를 우리 서비스 규격으로 통일하는 DTO입니다.
    private Map<String, Object> attributes; // 소셜 서버에서 받은 전체 원본 데이터
    private String nameAttributeKey; // OAuth2 로그인 시 키가 되는 필드값 (PK 역할)
    private String name; // 사용자 이름 (또는 닉네임)
    private String email; // 사용자 이메일

    @Builder
    public OAuth2Attributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    /**
     * registrationId(google, kakao, naver)를 구분하여 각 서비스에 맞는 추출 메서드를 호출합니다.
     */
    public static OAuth2Attributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            // [수정] "id" 대신 userNameAttributeName(이미 properties에서 "response"로 설정됨)을 그대로 전달합니다.
            return ofNaver(userNameAttributeName, attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    /**
     * 구글에서 받은 정보를 매핑합니다.
     */
    private static OAuth2Attributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuth2Attributes.builder()
                .name((String) attributes.get("name")) // 구글은 'name' 키를 사용합니다.
                .email((String) attributes.get("email")) // 구글은 'email' 키를 사용합니다.
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /**
     * 네이버에서 받은 정보를 매핑합니다.
     * application.properties의 'user-name-attribute=response' 설정 덕분에
     * attributes 파라미터는 이미 'response' 객체 내부의 데이터 맵입니다.
     */
    private static OAuth2Attributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2Attributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                // [중요] 반드시 원본 attributes를 넘겨주어야 SuccessHandler가 정상 작동합니다.
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName) // 여기에는 "response"가 담깁니다.
                .build();
    }

    /**
     * 카카오에서 받은 정보를 매핑합니다.
     */
    private static OAuth2Attributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (kakaoAccount != null) ? (Map<String, Object>) kakaoAccount.get("profile") : null;

        return OAuth2Attributes.builder()
                .name(profile != null ? (String) profile.get("nickname") : "카카오 사용자")
                .email(kakaoAccount != null ? (String) kakaoAccount.get("email") : "")
                .attributes(attributes) // 핸들러에서 kakao_account를 꺼낼 수 있도록 전체 원본 유지
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
}