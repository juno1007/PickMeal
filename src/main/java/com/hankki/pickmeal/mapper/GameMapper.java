package com.hankki.pickmeal.mapper;

import com.hankki.pickmeal.domain.Game;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GameMapper {
    // 게임 결과를 game 테이블에 저장합니다.
    void insertGameRecord(Game game);
}