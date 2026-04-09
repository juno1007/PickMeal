package com.hankki.pickmeal.scheduler;

import com.hankki.pickmeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserStatusScheduler {

    private final UserMapper userMapper;

    // 🚩 1분마다 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoReleaseSuspensions() {
        int count = userMapper.releaseExpiredSuspensions();
        if (count > 0) {
            System.out.println("### [스케줄러] 정지 기간이 만료된 유저 " + count + "명을 활성화했습니다.");
        }
    }
}