package PickMeal.PickMeal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException; // 🚩 정지된 계정 예외 처리를 위해 추가!
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 1. 기본 메시지 설정
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        // 2. 디버깅을 위해 콘솔에 에러 정보를 상세히 출력
        System.out.println("### Login Failure Exception: " + exception.getClass().getName());
        System.out.println("### Exception Message: " + exception.getMessage());

        // 🚩 3. 정지된 계정(SUSPENDED) 로그인 시도 처리
        if (exception instanceof LockedException) {
            errorMessage = exception.getMessage();
        }
        // 4. 소셜 로그인 예외(OAuth2AuthenticationException)인지 확인
        else if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthException =
                    (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception;

            String errorCode = oauthException.getError().getErrorCode();

            // 기존 탈퇴 메시지 처리 (유지)
            if ("withdrawn_user".equals(errorCode) || (exception.getMessage() != null && exception.getMessage().contains("withdrawn_user"))) {
                errorMessage = "탈퇴 처리된 계정입니다. 고객센터에 문의해주세요.";
            }
            // 🚩 [추가] 소셜 로그인 정지된 계정 처리
            else if ("suspended_user".equals(errorCode)) {
                errorMessage = "운영원칙 위반으로 이용이 정지된 계정입니다.";
            }
        }

        // 5. 세션에 메시지 저장
        request.getSession().setAttribute("errorMessage", errorMessage);

        // 6. 로그인 페이지로 리다이렉트
        response.sendRedirect(request.getContextPath() + "/users/login");
    }
}