package com.hankki.pickmeal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              @RequestParam(value = "status", required = false) Integer statusParam) {

        // 1. 스프링이 던져주는 실제 에러 코드 가져오기
        Object status = request.getAttribute(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = (status != null) ? Integer.parseInt(status.toString()) : 0;

        // 2. 만약 주소창에 직접 ?status=403 처럼 파라미터를 보냈다면 그 값을 우선 사용 (테스트용)
        if (statusParam != null) {
            statusCode = statusParam;
        }

        if (statusCode == 403) {
            return "error/403";
        } else if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 500) {
            return "error/500";
        }

        // 3. 403, 404, 500 외의 에러나 코드가 없을 때
        // 🚩 주의: 반드시 src/main/resources/templates/error/error.html 파일이 있어야 합니다!
        return "error/error";
    }
}