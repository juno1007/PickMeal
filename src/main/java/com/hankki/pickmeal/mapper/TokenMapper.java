package com.hankki.pickmeal.mapper;

import com.hankki.pickmeal.domain.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TokenMapper {
    // 1. 새로운 토큰 저장
    void saveToken(PasswordResetToken token);

    // 2. 토큰 값으로 정보 조회
    PasswordResetToken findByToken(String token);

    // 3. 사용 완료된 토큰 삭제 (혹은 만료된 토큰 삭제)
    void deleteToken(String token);
}
