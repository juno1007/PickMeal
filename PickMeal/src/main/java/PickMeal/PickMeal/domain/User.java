package PickMeal.PickMeal.domain;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Data
public class User implements UserDetails { // 시큐리티 연동을 위해 UserDetails 구현
    private Long user_id; // PK
    private String id; // 로그인 아이디
    private String password; // 비밀번호
    private String nickname; // 별명
    private String name; // 이름
    private String email; // 이메일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthDate; // 생년월일
    private String gender; // 성별
    private String phoneNumber; // 연락처
    private String likeMenu; // 좋아하는 음식
    private String disLikeMenu; // 싫어하는 음식
    private Date joinDate; // 가입일
    private String role; // 권한 (ROLE_USER 등)
    private String status; // 상태 (ACTIVE, WITHDRAWN)
    private String socialLoginSite; // 소셜 사이트 이름
    private String socialId; // 소셜 고유 식별값


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role)); // 권한 반환
    }

    @Override
    public String getUsername() {
        return this.id;
    } // 아이디 반환

    @Override
    public boolean isAccountNonExpired() {
        return true;
    } // 계정 만료 여부

    @Override
    public boolean isAccountNonLocked() {
        return true;
    } // 계정 잠금 여부

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    } // 비번 만료 여부

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(this.status);
    } // 활성화 여부
}
