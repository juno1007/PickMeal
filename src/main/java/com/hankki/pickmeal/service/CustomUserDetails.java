package com.hankki.pickmeal.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final String name;   // DB의 실명
    private final String status; // 🚩 DB의 상태(ACTIVE, SUSPENDED 등)를 담을 공간 추가!

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             String name, String status) { // 🚩 파라미터에 status 추가
        super(username, password, authorities);
        this.name = name;
        this.status = status; // 🚩 내 공간에 상태 저장
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 🚩 이제 내가 가지고 있는 status 변수를 비교하면 됩니다!
        return !"SUSPENDED".equals(this.status);
    }
}