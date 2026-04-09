package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 관리자만 접근 가능
public class AdminController {

    private final UserService userService;

    // 1. 관리자 회원 관리 페이지 조회 & 검색
    // AdminController.java 내부 1번 메서드 교체
    @GetMapping("/users")
    public String adminUsersPage(@RequestParam(required = false) String searchKeyword, Model model) {

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // 🚩 닉네임으로 회원 목록 찾기
            List<User> users = userService.searchByNickname(searchKeyword);
            model.addAttribute("searchedUsers", users);

            if (users.isEmpty()) {
                model.addAttribute("errorMessage", "해당 닉네임이 포함된 회원이 없습니다.");
            }
        }
        return "admin/user-manage";
    }

    // 2. 회원 정지 처리 (POST)
    @PostMapping("/users/suspend")
    public String suspendUser(@RequestParam Long user_id, @RequestParam int days, RedirectAttributes rttr) {
        userService.suspendUser(user_id, days);
        rttr.addFlashAttribute("successMessage", days + "일 정지 처리가 완료되었습니다.");
        return "redirect:/admin/users";
    }

    // 3. 회원 정지 즉시 해제 (POST)
    @PostMapping("/users/unsuspend")
    public String unsuspendUser(@RequestParam Long user_id, RedirectAttributes rttr) {
        userService.unsuspendUser(user_id);
        rttr.addFlashAttribute("successMessage", "회원 정지가 해제되었습니다.");
        return "redirect:/admin/users";
    }
}