package com.hankki.pickmeal.service;

import com.hankki.pickmeal.domain.PasswordResetToken; // 엔티티 경로 확인
import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.mapper.TokenMapper; // 인터페이스 이름 일치
import com.hankki.pickmeal.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserPasswordService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenMapper tokenMapper; // 학생님이 만드신 이름 그대로 사용

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService mailService; // 메일 발송 서비스 (이미 있다고 가정)

    /**
     * 비밀번호 재설정 링크를 메일로 발송하는 핵심 로직
     */
    @Transactional
    public String sendResetLink(String id, String email) {
        // 1. [본인 확인] 아이디와 이메일이 일치하는 유저가 있는지 조회
        User user = userMapper.findUserByIdAndEmail(id, email);

        if (user == null) {
            return "not_found"; // 일치하는 유저 정보 없음
        }

        // 2. [토큰 생성] 추측 불가능한 무작위 보안 토큰 생성 (UUID)
        String tokenValue = UUID.randomUUID().toString();

        // 3. [토큰 객체 구성] PasswordResetToken 엔티티에 정보 담기
        PasswordResetToken tokenObj = new PasswordResetToken();
        tokenObj.setUserId(user.getUser_id()); // 유저의 PK값
        tokenObj.setToken(tokenValue);
        // 만료 시간은 현재로부터 30분 후 (java.time.LocalDateTime 사용)
        tokenObj.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        // 4. [토큰 저장] 별도 토큰 테이블에 INSERT
        tokenMapper.saveToken(tokenObj);

        // 5. [메일 발송] 사용자에게 재설정 페이지 링크 전송
        String resetLink = "http://localhost:8080/users/reset-password?token=" + tokenValue;

        // 메일 본문 구성 (인증 유효 시간 안내 포함)
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요! Pick Meal입니다.\n\n");
        sb.append("비밀번호 재설정을 위한 링크를 안내해 드립니다.\n");
        sb.append("아래 링크를 클릭하여 비밀번호를 새로 설정해 주세요.\n\n");
        sb.append("[ 비밀번호 재설정 링크 ]\n").append(resetLink).append("\n\n");

        sb.append("⚠️ 해당 링크의 유효 시간은 발송 후 [ 30분 ] 입니다.\n");
        sb.append("30분이 지나면 링크가 만료되어 다시 요청하셔야 합니다.\n\n");
        sb.append("본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.");

        // [중요] 메서드 이름을 EmailService에서 만든 이름과 똑같이 맞춥니다.
        mailService.sendPasswordResetMail(
                user.getEmail(),
                "[Pick Meal] 비밀번호 재설정 안내입니다. ✉️",
                sb.toString()
        );

        return "success";
    }

    // 1. 토큰 유효성 검증 (GET 요청 시 사용)
    public boolean validatePasswordResetToken(String token) {
        PasswordResetToken resetToken = tokenMapper.findByToken(token);

        // 토큰이 존재하지 않거나, 만료 시간이 현재 시간보다 이전이면 false 반환
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        return true;
    }

    // 2. 실제 비밀번호 변경 (POST 요청 시 사용)
    @Transactional
    public boolean updatePassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenMapper.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 2. [핵심] 사용자가 입력한 평문 비번을 암호화(Hashing)합니다.
        String encryptedPassword = passwordEncoder.encode(newPassword);

        // 3. 암호화된 비밀번호를 DB에 저장합니다.
        userMapper.updatePassword(resetToken.getUserId(), encryptedPassword);

        // 4. 사용한 토큰 삭제
        tokenMapper.deleteToken(token);

        return true;
    }
}