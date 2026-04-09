package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.service.UserPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
public class PasswordResetController {

    @Autowired
    private UserPasswordService userPasswordService;

    // 1. 메일 링크를 클릭했을 때 호출되는 메서드
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // 토큰이 유효한지(존재하는지, 만료되지 않았는지) 서비스에서 확인
        boolean isValid = userPasswordService.validatePasswordResetToken(token);

        if (!isValid) {
            return "redirect:/login?error=invalid_token"; // 유효하지 않으면 로그인 페이지로 튕김
        }

        model.addAttribute("token", token); // HTML 폼에 토큰을 숨겨서 전달하기 위해 담음
        return "users/reset-password-form"; // 비밀번호 변경 HTML 페이지
    }

    // 2. 새 비밀번호를 제출했을 때 호출되는 메서드
    @PostMapping("/reset-password")
    @ResponseBody
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("newPassword") String newPassword) {
        boolean result = userPasswordService.updatePassword(token, newPassword);
        return result ? "success" : "fail";
    }

    @PostMapping("/find-password/send-link")
    @ResponseBody
    public String handleFindPassword(@RequestParam("id") String id,
                                     @RequestParam("email") String email) {

        // 2. 클래스 이름(대문자)이 아니라, 위에서 선언한 변수명(소문자)으로 호출합니다!
        // UserPasswordService.sendResetLink(id, email) -> (X)
        String result = userPasswordService.sendResetLink(id, email); // (O)

        return result;
    }
}
