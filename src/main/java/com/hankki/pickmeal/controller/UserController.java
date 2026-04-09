package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.Board;
import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.dto.PlaceStatsDto;
import com.hankki.pickmeal.service.BoardService;
import com.hankki.pickmeal.service.RestaurantService;
import com.hankki.pickmeal.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final BoardService boardService;
    private final UserService userService;
    private final RestaurantService restaurantService;
    private final PasswordEncoder passwordEncoder;

    /**
     * [핵심] 소셜/일반 로그인 통합 ID 추출 메서드
     * 네이버, 카카오, 구글의 각기 다른 데이터 구조를 분석하여 DB용 ID를 반환합니다.
     */
    private String getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String registrationId = oauthToken.getAuthorizedClientRegistrationId(); // google, kakao, naver
            OAuth2User oAuth2User = oauthToken.getPrincipal();
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String socialId = "";

            try {
                if ("naver".equals(registrationId)) {
                    Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                    socialId = String.valueOf(response.get("id"));
                } else if ("kakao".equals(registrationId)) {
                    // 카카오는 id가 Long이거나 Integer일 수 있으므로 확실하게 처리
                    Object id = attributes.get("id");
                    socialId = (id != null) ? id.toString() : "";
                } else if ("google".equals(registrationId)) {
                    // 구글은 sub가 핵심 ID입니다.
                    Object sub = attributes.get("sub");
                    socialId = (sub != null) ? sub.toString() : "";
                }

                // 추출한 ID가 비어있다면 최후의 수단으로 getName() 사용
                if (socialId.isEmpty() || "null".equals(socialId)) {
                    socialId = oAuth2User.getName();
                }
            } catch (Exception e) {
                // 에러 발생 시 로그를 남기고 getName()으로 방어
                System.out.println("소셜 ID 추출 중 오류 발생: " + e.getMessage());
                socialId = oAuth2User.getName();
            }

            return registrationId + "_" + socialId;
        }

        return authentication.getName(); // 일반 로그인
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User()); // 일반 가입 시 빈 유저 객체 바인딩
        model.addAttribute("isSocial", false); // 프론트에서 소셜 유저임을 판별
        return "users/signup";
    }

    @PostMapping("/signup")
    public String signUp(User user, RedirectAttributes rttr){
        try {
            userService.signUp(user); // 가입 로직 호출
            rttr.addFlashAttribute("msg", "회원가입이 완료되었습니다.");
            return "redirect:/users/login";
        } catch (IllegalStateException e){
            rttr.addFlashAttribute("msg", e.getMessage()); // 중복 가입 에러 메시지 전달
            return "redirect:/users/signup";
        }
    }

    @GetMapping("/check-id")
    @ResponseBody // JSON/문자열 데이터만 반환
    public String checkId(@RequestParam("id") String id) {
        if (id == null || id.trim().isEmpty()) return "invalid";
        return userService.isIdDuplicate(id) ? "fail" : "success"; // 중복 검사 결과 반환
    }

    @GetMapping("/check-nickname")
    @ResponseBody
    public String checkNicknameDuplication(@RequestParam("nickname") String nickname) {
        // 1. 빈 값 검사
        if (nickname == null || nickname.trim().isEmpty()) {
            return "fail";
        }

        // 2. 서비스 로직 호출 (중복이면 true, 사용 가능하면 false 반환됨)
        boolean isExist = userService.existsByNickname(nickname);

        // 3. 자바스크립트에서 처리하기 쉽게 문자열로 변환하여 응답
        return isExist ? "fail" : "success";
    }

    @GetMapping("/signup/social")
    public String socialSignupForm(
            @RequestParam("socialId") String socialId,
            @RequestParam("email") String email,
            @RequestParam("site") String site,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "nickname", required = false) String nickname, // nickname 추가
            Model model) {

        User socialUser = new User();
        socialUser.setSocialId(socialId);
        socialUser.setSocialLoginSite(site);

        // 네이버나 카카오는 nickname으로 올 확률이 높으므로 방어 로직 추가
        String finalNickname = (nickname != null && !nickname.isEmpty()) ? nickname : name;
        socialUser.setNickname(finalNickname);

        socialUser.setEmail(email);
        socialUser.setName(name); // 이름 필드도 있다면 채워줌

        // CustomOAuth2UserService에서 만든 규칙과 동일하게 ID 세팅
        socialUser.setId(site + "_" + socialId);

        model.addAttribute("user", socialUser);
        model.addAttribute("isSocial", true);

        return "users/signup";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            HttpSession session, Model model) {

        // 일반 로그인 실패 시 시큐리티가 세션에 담아둔 에러 메시지 확인
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }

        // [추가] 소셜 로그인 실패(탈퇴 회원 등) 시 URL 파라미터로 넘어오는 에러 처리
        // 시큐리티 설정에 따라 에러가 발생하면 /login?error 형태로 이동합니다.
        if (error != null) {
            model.addAttribute("errorMessage", "탈퇴 처리된 계정입니다. 고객센터에 문의해주세요.");
        }

        return "users/login";
    }

    @GetMapping("/mypage")
    public String mypage(Authentication authentication, Model model) {
        // 1. 현재 로그인한 유저의 기본 식별자(ID)를 가져옵니다. (예: 4746951582)
        String userId = getLoginUserId(authentication);
        if (userId == null) return "redirect:/users/login";

        // 2. 소셜 로그인 종류(google, kakao, naver)를 가져옵니다.
        String registrationId = "";
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            registrationId = ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        }

        // 3. [수정] 이미 접두어가 붙어있는지 확인하고 조합합니다.
        String fullUserId;
        if (!registrationId.isEmpty() && userId.startsWith(registrationId)) {
            // 이미 "kakao_" 등으로 시작한다면 그대로 사용
            fullUserId = userId;
        } else {
            // 일반 로그인이거나 접두어가 없는 경우에만 붙여줌
            fullUserId = registrationId.isEmpty() ? userId : registrationId + "_" + userId;
        }

        System.out.println("최종 DB 조회 ID: " + fullUserId); // 여기서 kakao_4746951582 가 나와야 합니다!

        // 4. 조합된 fullUserId로 DB에서 최신 유저 정보를 가져옵니다.
        User latestUserInfo = userService.findById(fullUserId);

        // [디버깅] 유저를 못 찾을 경우 콘솔에 로그를 남겨 원인을 파악합니다.
        if (latestUserInfo == null) {
            System.out.println("마이페이지 조회 실패 - DB에 없는 ID로 접근 시도: " + fullUserId);
            return "redirect:/users/login";
        }

        // 5. 내가 작성한 게시글 리스트 가져오기 (User의 long 타입 고유 번호 PK 사용)
        List<Board> myBoards = boardService.findByUser_id(latestUserInfo.getUser_id());

        // latestUserInfo.getId()가 "aaa" 또는 "kakao_..." 입니다.
        List<PlaceStatsDto> likeList = restaurantService.getMyLikedResturants(fullUserId);
        // 아래 로그 추가
        System.out.println(">>> 가져온 찜 목록 개수: " + (likeList != null ? likeList.size() : 0));

        // 6. 뷰(HTML)로 전달할 데이터들을 모델에 담습니다.
        model.addAttribute("user", latestUserInfo);
        model.addAttribute("displayId", userService.getMaskedDisplayId(latestUserInfo));
        model.addAttribute("myBoards", myBoards);
        model.addAttribute("likeList", likeList);

        return "users/mypage";
    }

    @GetMapping("/edit")
    public String editForm(Authentication authentication, Model model) {
        String userId = getLoginUserId(authentication);
        if (userId == null) return "redirect:/users/login";

        User user = userService.findById(userId);
        if (user == null) return "redirect:/users/login";

        // [리팩토링] 서비스의 공통 메서드 호출
        String displayId = userService.getMaskedDisplayId(user);

        model.addAttribute("user", user);
        model.addAttribute("displayId", displayId);
        return "users/edit";
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute User user, RedirectAttributes rttr, Authentication authentication) {
        // [중요] 폼의 hidden id 대신 인증 세션의 ID를 사용하여 보안 강화
        String userId = getLoginUserId(authentication);
        User existingUser = userService.findById(userId);

        if (existingUser == null) {
            rttr.addFlashAttribute("msg", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/users/mypage";
        }

        // DB 원본 데이터 병합 (마스킹 데이터 방어)
        user.setPassword(existingUser.getPassword());
        user.setEmail(existingUser.getEmail());
        user.setName(existingUser.getName());
        user.setPhoneNumber(existingUser.getPhoneNumber());
        user.setSocialLoginSite(existingUser.getSocialLoginSite());
        user.setSocialId(existingUser.getSocialId());
        user.setRole(existingUser.getRole());
        user.setStatus(existingUser.getStatus());

        userService.edit(user, false);
        rttr.addFlashAttribute("msg", "정보 수정이 완료되었습니다.");
        return "redirect:/users/mypage";
    }

    /**
     * 팝업창 이메일 즉시 변경 API
     * @RequestBody를 사용하여 JSON 데이터를 User 객체로 매핑합니다.
     */
    @PostMapping("/update-email")
    @ResponseBody
    public ResponseEntity<String> updateEmail(@RequestBody User userRequest, Authentication authentication) {
        try {
            String userId = getLoginUserId(authentication);
            User loginUser = userService.findById(userId);

            if (loginUser == null) return ResponseEntity.status(401).body("unauthorized");

            // 1. 소셜 로그인 사용자인지 확인
            if (loginUser.getSocialLoginSite() != null && !loginUser.getSocialLoginSite().isEmpty()) {
                return ResponseEntity.status(403).body("social_user_cannot_change_email");
            }

            // 2. [추가] 이메일 중복 검사
            // 수정하려는 이메일이 이미 DB에 존재하는지 확인합니다.
            User existingUser = userService.findByEmail(userRequest.getEmail());
            if (existingUser != null) {
                // 본인이 현재 사용 중인 이메일일 수도 있으므로 ID까지 체크하면 더 정확합니다.
                // String으로 변환하여 비교
                // Objects.equals는 null 체크까지 포함하여 안전합니다.
                if (!Objects.equals(existingUser.getUser_id(), loginUser.getUser_id())) {
                    return ResponseEntity.status(409).body("already_exists");
                }
            }

            userService.updateEmail(loginUser.getUser_id(), userRequest.getEmail());
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("fail");
        }
    }

    /**
     * 팝업창 비밀번호 즉시 변경 API
     */
    @PostMapping("/update-password")
    @ResponseBody
    public String updatePassword(@RequestBody java.util.Map<String, String> params,
                                 Authentication authentication) {
        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");

        // 1. 유저 조회
        String userId = getLoginUserId(authentication);
        User user = userService.findById(userId);
        if (user == null) return "fail";

        // 2. 소셜 유저 체크
        if (user.getSocialLoginSite() != null && !user.getSocialLoginSite().isEmpty()) {
            return "social_user_cannot_change_pw";
        }

        // 3. 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return "current_password_incorrect";
        }

        // 4. [추가] 새 비밀번호 복잡도 검증 (영문+숫자+특수문자 8자 이상)
        String pwPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        if (newPassword == null || !newPassword.matches(pwPattern)) {
            return "invalid_password_format";
        }

        try {
            userService.updatePassword(user.getUser_id(), newPassword);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }

    @GetMapping("/find-id")
    public String findIdPage() {
        return "users/find-id";
    }

    @PostMapping("/find-id")
    @ResponseBody // 결과값만 브라우저로 보낼 때 사용합니다.
    public ResponseEntity<String> findId(@RequestParam String name, @RequestParam String email) {
        String maskedId = userService.findId(name, email); // 아까 만든 마스킹 로직이 포함된 메서드 호출

        if (maskedId == null) {
            // 일치하는 유저가 없을 때 404 에러나 특정 문자열을 보냅니다.
            return ResponseEntity.status(404).body("not_found");
        }
        return ResponseEntity.ok(maskedId);
    }

    @PostMapping("/remove")
    public String remove(Authentication authentication, HttpSession session, RedirectAttributes rttr) {
        // 1. 인증 객체 체크
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        try {
            // [핵심 수정] 우리가 만든 공통 메서드로 실제 DB용 ID를 가져옵니다.
            String userId = getLoginUserId(authentication);
            User user = userService.findById(userId);

            if (user != null) {
                // 2. 서비스 호출: status를 'WITHDRAWN'으로 변경 (Soft Delete)
                userService.remove(user.getUser_id());

                // 3. 세션 무효화 및 시큐리티 컨텍스트 클리어 (로그아웃 처리)
                session.invalidate();
                // 시큐리티 권한 정보도 완전히 삭제하는 것이 안전합니다.
                org.springframework.security.core.context.SecurityContextHolder.clearContext();

                rttr.addFlashAttribute("msg", "회원 탈퇴가 정상적으로 처리되었습니다.");
                return "redirect:/";
            } else {
                rttr.addFlashAttribute("msg", "유저 정보를 찾을 수 없어 탈퇴에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("msg", "탈퇴 처리 중 오류가 발생했습니다.");
        }

        return "redirect:/users/mypage";
    }

}