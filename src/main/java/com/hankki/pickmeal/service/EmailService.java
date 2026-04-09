package com.hankki.pickmeal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // 공통 인사말 가이드
    private final String COMMON_GREETING = "안녕하세요! Pick Meal입니다.\n\n";

    public String createCode() {
        return String.valueOf((int)(Math.random() * 899999) + 100000);
    }

    /**
     * 2. [인증용] 회원가입(JOIN) 또는 수정(EDIT) 시 인증번호 발송
     */
    public void sendMail(String toEmail, String code, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom("PickMeal <" + fromEmail + ">");

        StringBuilder sb = new StringBuilder(COMMON_GREETING); // 인사말 추가

        if ("EDIT".equals(type)) {
            message.setSubject("[Pick Meal] 프로필 수정 인증번호입니다. ✉️");
            sb.append("요청하신 프로필 수정 인증번호는 [").append(code).append("] 입니다.");
        } else {
            message.setSubject("[Pick Meal] 회원가입 인증번호입니다. ✉️");
            sb.append("요청하신 회원가입 인증번호는 [").append(code).append("] 입니다.");
        }

        message.setText(sb.toString());
        mailSender.send(message);
    }

    /**
     * 3. [변경] 비밀번호 재설정용 메서드
     * 본문 앞에 공통 인사말을 자동으로 붙여줍니다.
     */
    public void sendPasswordResetMail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);

        // 서비스에서 넘어온 내용(content) 앞에 인사말을 합칩니다.
        String finalContent = COMMON_GREETING + content;
        message.setText(finalContent);

        message.setFrom("PickMeal <" + fromEmail + ">");

        mailSender.send(message);
    }
}
