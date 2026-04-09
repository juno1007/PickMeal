package PickMeal.pickmeal; // 프로젝트 패키지 경로에 맞게 수정하세요.

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    @Test // JUnit을 이용해 실행 버튼을 활성화합니다.
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin"; // 원하는 비밀번호
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("================================");
        System.out.println("암호화된 비밀번호: " + encodedPassword);
        System.out.println("================================");
    }
}