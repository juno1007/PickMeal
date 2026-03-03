package PickMeal.PickMeal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 2. Principal(인증 주체)을 가져옵니다.
        // 우리가 UserDetailsService에서 return한 객체가 여기 들어있습니다.
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            // 시큐리티 세션에 저장된 정보를 "loginUser"라는 이름으로 세션에 공유
            // (이미 인증 시점에 DB에서 정보를 다 긁어왔기 때문에 여기서 서비스를 또 부를 필요가 없습니다.)
            session.setAttribute("loginUser", principal);
        }

        setDefaultTargetUrl("/next-page");
        setAlwaysUseDefaultTargetUrl(true);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}