package com.hankki.pickmeal.service;

import com.hankki.pickmeal.domain.Food;
import com.hankki.pickmeal.domain.Game;
import com.hankki.pickmeal.mapper.GameMapper;
import com.hankki.pickmeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameService {

    private final GameMapper gameMapper;
    private final UserMapper userMapper;

    /**
     * [설명] 사용자의 선호/비선호를 모두 반영하여 월드컵 게임 리스트를 만듭니다.
     * @param userId 유저 고유 ID (비로그인 시 null)
     * @param allFoods DB에서 가져온 전체 음식 리스트
     * @param round 게임 라운드 (8, 16, 32, 64 등)
     */
    public List<Food> getPriorityFoodList(Long userId, List<Food> allFoods, int round) {

        // 1. 유저의 취향 데이터 가져오기 (비유: 블랙리스트와 화이트리스트 장부 확인)
        String dislikedMenuString = "";
        String likedMenuString = "";

        if (userId != null) {
            dislikedMenuString = userMapper.getDislikedMenuString(userId);
            likedMenuString = userMapper.getLikedMenuString(userId); // [필수] UserMapper에 이 메서드가 있어야 합니다.
        }

        final String dislikedTags = (dislikedMenuString != null) ? dislikedMenuString : "";
        final String likedTags = (likedMenuString != null) ? likedMenuString : "";

        log.info("--- [서비스 체크] 유저 ID: {}, 비선호: [{}], 선호: [{}]", userId, dislikedTags, likedTags);

        // 2. [검수기 설정] 싫어하는 음식을 걸러내는 '거름망(Filter)' 로직
        Predicate<Food> isSafeFood = food -> {
            if (dislikedTags.isEmpty()) return true;

            String[] tags = dislikedTags.split(",");
            String fName = (food.getFoodName() != null) ? food.getFoodName().replace(" ", "").toLowerCase() : "";
            String fDesc = (food.getDescription() != null) ? food.getDescription().replace(" ", "").toLowerCase() : "";
            String fFull = fName + fDesc;

            for (String tag : tags) {
                String cleanedTag = tag.trim().toLowerCase();
                if (cleanedTag.isEmpty()) continue;

                List<String> banList = new ArrayList<>();
                String simplifiedTag = cleanedTag.replace(" ", "");
                banList.add(simplifiedTag);

                // [보강] '날것' 관련 확장 필터
                if (simplifiedTag.contains("날것")) {
                    banList.addAll(Arrays.asList("카이센동", "사케동", "육회", "스시", "초밥", "사시미", "회덮밥", "신선", "생선", "생고기"));
                }
                // [보강] '면' 관련 확장 필터
                if (simplifiedTag.contains("면")) {
                    banList.addAll(Arrays.asList("국수", "스파게티", "파스타", "우동", "소바", "라멘", "짬뽕", "짜장", "자장", "칼국수", "막국수"));
                }
                // [보강] '해산물' 관련 확장 필터
                if (simplifiedTag.contains("해산물") || simplifiedTag.contains("생선")) {
                    banList.addAll(Arrays.asList("조개", "새우", "게장", "매운탕", "회", "해물", "생선", "고등어", "갈치", "오징어"));
                }

                for (String banWord : banList) {
                    if (fFull.contains(banWord)) {
                        log.info("#### [차단 완료] '{}' 탈락! (사유: {})", food.getFoodName(), banWord);
                        return false;
                    }
                }
            }
            return true;
        };

        // 3. [1단계: 필터링] 안전한 음식만 1차 선별
        List<Food> filteredFoods = new ArrayList<>();
        for (Food f : allFoods) {
            if (isSafeFood.test(f)) {
                filteredFoods.add(f);
            }
        }

        // 4. [2단계: 우선순위 배치] 선호 음식을 앞으로 모으기
        List<Food> priorityGroup = new ArrayList<>(); // VIP 그룹
        List<Food> normalGroup = new ArrayList<>();   // 일반 그룹

        if (!likedTags.isEmpty()) {
            String[] likedArray = likedTags.split(",");

            for (Food f : filteredFoods) {
                boolean isLiked = false;
                String fFull = (f.getFoodName() + f.getDescription()).replace(" ", "").toLowerCase();

                for (String lTag : likedArray) {
                    String cleanedLTag = lTag.trim().replace(" ", "").toLowerCase();
                    if (!cleanedLTag.isEmpty() && fFull.contains(cleanedLTag)) {
                        isLiked = true;
                        break;
                    }
                }

                if (isLiked) {
                    priorityGroup.add(f); // 선호 키워드 포함 시 VIP행
                } else {
                    normalGroup.add(f);    // 그 외에는 일반행
                }
            }
        } else {
            // 좋아하는 태그가 없으면 모두 일반 그룹으로
            normalGroup.addAll(filteredFoods);
        }

        // 5. [섞기] 각 그룹 내에서 무작위로 섞어줍니다 (매번 같은 순서 방지)
        Collections.shuffle(priorityGroup);
        Collections.shuffle(normalGroup);

        // 6. [합치기] VIP(선호) 음식을 리스트의 맨 앞부분에 배치
        List<Food> finalResult = new ArrayList<>();
        finalResult.addAll(priorityGroup);
        finalResult.addAll(normalGroup);

        // 7. [자르기] 라운드 수(예: 16)만큼만 최종 선발
        int finalLimit = Math.min(finalResult.size(), round);
        log.info("--- [최종 통계] 선호음식: {}개, 일반음식: {}개 선발됨",
                Math.min(priorityGroup.size(), finalLimit),
                Math.max(0, finalLimit - priorityGroup.size()));

        return new ArrayList<>(finalResult.subList(0, finalLimit));
    }

    public void insertGameRecord(Game game) {
        if (game.getUser_id() != null && game.getUser_id() == 0L) {
            game.setUser_id(null);
        }
        gameMapper.insertGameRecord(game);
    }
}