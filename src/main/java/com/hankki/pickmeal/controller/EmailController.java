package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.service.EmailService;
import com.hankki.pickmeal.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final UserService userService; // [추가] 유저 존재 여부 확인을 위해 주입

    // 1. 인증 메일 발송 요청 처리
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestParam("email") String email,
            @RequestParam(value = "type", defaultValue = "JOIN") String type,
            HttpSession session) {

        // [핵심 추가] 프로필 수정(EDIT) 시에도 중복 체크를 먼저 수행합니다.
        User existingUser = userService.findByEmail(email);
        if (existingUser != null) {
            // 이미 가입된 계정이 소셜인지 일반인지 구분해서 응답하면 더 친절합니다.
            if (existingUser.getSocialLoginSite() != null && !existingUser.getSocialLoginSite().isEmpty()) {
                return ResponseEntity.status(409).body("social_" + existingUser.getSocialLoginSite());
            }
            return ResponseEntity.status(409).body("already_exists"); // 409 Conflict 에러 반환
        }

        try {
            // 중복이 없을 때만 아래 로직(메일 발송)이 실행됩니다.
            String code = emailService.createCode();
            session.setAttribute("emailCode", code);
            session.setAttribute("sendTime", System.currentTimeMillis());

            emailService.sendMail(email, code, type);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("error");
        }
    }

    // 2. 인증번호 확인 로직 (기존과 동일하되 반환 타입만 통일)
    @PostMapping("/verify")
    public String verifyCode(@RequestParam("code") String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("emailCode");
        Long sendTime = (Long) session.getAttribute("sendTime");

        if (savedCode == null || sendTime == null) return "expired";

        long currentTime = System.currentTimeMillis();
        if (currentTime - sendTime > 5 * 60 * 1000) {
            session.removeAttribute("emailCode");
            session.removeAttribute("sendTime");
            return "timeout";
        }

        if (savedCode.equals(code)) {
            session.removeAttribute("emailCode");
            session.removeAttribute("sendTime");
            return "success";
        }
        return "fail";
    }
}