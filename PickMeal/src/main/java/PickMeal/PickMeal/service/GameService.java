package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Food;
import PickMeal.PickMeal.domain.Game;
import PickMeal.PickMeal.mapper.GameMapper;
import PickMeal.PickMeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameService {

    private final GameMapper gameMapper;
    private final UserMapper userMapper;

    /**
     * [설명] 사용자의 선호도를 반영하여 게임에 사용할 음식 리스트를 만듭니다.
     * @param userId 유저 고유 ID (비로그인 시 null)
     * @param allFoods 133개의 전체 음식 리스트
     * @param round 게임 라운드 (8, 16, 32 등)
     */
    public List<Food> getPriorityFoodList(Long userId, List<Food> allFoods, int round) {
        List<Food> selectedFoods = new java.util.ArrayList<>();

        // 1. 유저의 비선호 키워드 가져오기
        String dislikedMenuString = "";
        if (userId != null) {
            dislikedMenuString = userMapper.getDislikedMenuString(userId);
        }

        final String dislikedTags = (dislikedMenuString != null) ? dislikedMenuString : "";
        log.info("--- [서비스 체크] 유저 ID: {}, 비선호 키워드: {}", userId, dislikedTags);

        // 2. 필터링 조건 설정 (Predicate: 합격/불합격을 결정하는 검수기)
        java.util.function.Predicate<Food> isSafeFood = food -> {
            if (dislikedTags == null || dislikedTags.isEmpty()) return true;

            // 1. 유저의 싫어하는 태그들 (예: "오이, 날것")
            String[] tags = dislikedTags.split(",");

            // 2. 음식의 이름과 설명을 미리 청소해둡니다.
            String fName = (food.getFoodName() != null) ? food.getFoodName().replace(" ", "").toLowerCase() : "";
            String fDesc = (food.getDescription() != null) ? food.getDescription().replace(" ", "").toLowerCase() : "";
            String fFull = fName + fDesc;

            for (String tag : tags) {
                // 3. 태그 청소 (앞뒤 공백 제거)
                String cleanedTag = tag.trim().toLowerCase();
                if (cleanedTag.isEmpty()) continue;

                // 금지어 리스트 생성
                java.util.List<String> banList = new java.util.ArrayList<>();
                banList.add(cleanedTag.replace(" ", "")); // "날 것" -> "날것"

                // [핵심 보강] 날것이 포함된 모든 경우의 수를 강제로 집어넣습니다.
                if (cleanedTag.contains("날것")) {
                    // 학생님 DB에 '날것'이라는 글자가 없으니, '날것' 태그를 선택하면 아래 음식을 '강제'로 금지어에 넣습니다.
                    banList.addAll(java.util.Arrays.asList("카이센동", "사케동", "육회", "스시", "초밥", "사시미", "회덮밥", "신선", "생선"));
                }

                if (cleanedTag.contains("오이")) {
                    banList.addAll(java.util.Arrays.asList("냉면", "cucumber", "피클"));
                }

                // [신규] 면 처리 (추가된 부분!)
                if (cleanedTag.contains("면")) {
                    // "면"이라는 키워드를 선택했을 때, 실제 DB 음식 이름에 "면"이 없어도 필터링하고 싶은 메뉴들입니다.
                    banList.addAll(java.util.Arrays.asList(
                            "국수", "스파게티", "파스타", "우동", "소바", "라멘", "짬뽕", "짜장", "자장", "칼국수", "막국수"
                    ));
                    log.info("--- [면 필터 추가] 현재 금지어 명단: {}", banList);
                }


                // 4. 검사 시작
                for (String banWord : banList) {
                    if (fFull.contains(banWord)) {
                        log.info("#### [검거 완료] '{}' 차단! (선택태그: {}, 감지단어: {})",
                                food.getFoodName(), cleanedTag, banWord);
                        return false; // 하나라도 걸리면 이 음식은 여기서 즉시 끝! (탈락)
                    }
                }
            }
            return true; // 모든 태그 검사를 무사히 통과해야만 합격!
        };

        // 3. 필터링 시작 (133개 중 안전한 음식만 골라 담기)
        for (Food f : allFoods) {
            if (isSafeFood.test(f)) {
                selectedFoods.add(f);
            }
        }

        // 4. 머릿수(round) 확인
        if (selectedFoods.size() < round) {
            log.warn("--- [경고] 필터링 후 남은 음식이 {}개뿐이라 {}강을 채울 수 없습니다.", selectedFoods.size(), round);
        }

        // 5. 무작위로 섞기
        java.util.Collections.shuffle(selectedFoods);

        // 6. 라운드 수만큼만 잘라서 반환
        int finalLimit = Math.min(selectedFoods.size(), round);
        return new java.util.ArrayList<>(selectedFoods.subList(0, finalLimit));
    } // [수정된 부분] getPriorityFoodList 메서드의 끝을 알리는 중괄호!

    private List<String> parseMenuList(String menuStr) {
        if (menuStr == null || menuStr.trim().isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(menuStr.split("\\s*,\\s*")));
    }

    public void insertGameRecord(Game game) {
        if (game.getUser_id() != null && game.getUser_id() == 0L) {
            log.info("--- [GameService] 비회원(0) 처리 ---");
            game.setUser_id(null);
        }
        gameMapper.insertGameRecord(game);
    }
}