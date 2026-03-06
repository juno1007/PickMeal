package PickMeal.PickMeal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 1. 기본 메시지
        String errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        // 2. 예외 체인에서 LockedException 메시지를 찾아내는 함수 호출
        String suspendedMessage = findLockedExceptionMessage(exception);

        // 3. 정지 메시지가 발견되면 그것을 사용
        if (suspendedMessage != null) {
            errorMessage = suspendedMessage;
        }
        // 4. 소셜 로그인 예외 처리
        else if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException) {
            org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthException =
                    (org.springframework.security.oauth2.core.OAuth2AuthenticationException) exception;

            String errorCode = oauthException.getError().getErrorCode();
            if ("withdrawn_user".equals(errorCode)) {
                errorMessage = "탈퇴 처리된 계정입니다. 고객센터에 문의해주세요.";
            } else if ("suspended_user".equals(errorCode)) {
                errorMessage = exception.getMessage(); // 소셜용 정지 메시지
            }
        }

        // 5. 세션 저장 및 리다이렉트
        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/users/login");
    }

    /**
     * 예외를 꼬리물기(Cause)로 추적하여 LockedException의 메시지를 찾아내는 메서드
     */
    private String findLockedExceptionMessage(Throwable throwable) {
        if (throwable == null) return null;

        // LockedException을 발견하면 메시지 반환
        if (throwable instanceof LockedException) {
            return throwable.getMessage();
        }

        // 재귀 호출로 원인(Cause) 추적
        return findLockedExceptionMessage(throwable.getCause());
    }
}