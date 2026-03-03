package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Food;
import PickMeal.PickMeal.domain.Game;
import PickMeal.PickMeal.mapper.GameMapper;
import PickMeal.PickMeal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위해 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j // 로그 기능을 활성화합니다.
@Service
@RequiredArgsConstructor
@Transactional
public class GameService {

    private final GameMapper gameMapper;
    private final UserMapper userMapper;

    /**
     * 월드컵 리스트 생성 (선호 음식 포함, 비선호 제외)
     */
    public List<Food> getPriorityFoodList(Long userId, List<Food> allFoods, int round) {

        // 1. 비회원 처리
        if (userId == null) {
            Collections.shuffle(allFoods);
            return new ArrayList<>(allFoods.subList(0, Math.min(round, allFoods.size())));
        }

        // 2. 선호/비선호 리스트 가져오기
        String likedStr = userMapper.getLikedMenuString(userId);
        String dislikedStr = userMapper.getDislikedMenuString(userId);

        List<String> likedList = parseMenuList(likedStr);
        List<String> dislikedList = parseMenuList(dislikedStr);

        // 3. 좋아하는 음식 필터링 및 무작위 섞기
        List<Food> likedFoods = allFoods.stream()
                .filter(food -> likedList.contains(food.getFoodName()))
                .collect(Collectors.toList());
        Collections.shuffle(likedFoods);

        // 4. [핵심수정] 선호 음식이 강수(round)보다 많을 경우를 대비해 먼저 자릅니다.
        List<Food> selectedFoods = new ArrayList<>(likedFoods.subList(0, Math.min(round, likedFoods.size())));

        // 5. 부족한 개수만큼 일반 음식 채우기 (선호 음식과 비선호 음식 제외)
        int neededCount = round - selectedFoods.size();
        if (neededCount > 0) {
            List<Food> otherFoods = allFoods.stream()
                    .filter(food -> !likedList.contains(food.getFoodName()) && !dislikedList.contains(food.getFoodName()))
                    .collect(Collectors.toList());

            Collections.shuffle(otherFoods);
            selectedFoods.addAll(otherFoods.subList(0, Math.min(neededCount, otherFoods.size())));
        }

        // 최종 섞기
        Collections.shuffle(selectedFoods);
        return selectedFoods;
    }

    /**
     * 문자열 파싱 (NPE 방지 및 가변 리스트 반환)
     */
    private List<String> parseMenuList(String menuStr) {
        if (menuStr == null || menuStr.trim().isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(menuStr.split("\\s*,\\s*")));
    }

    /**
     * 게임 기록 저장 (Admin 통합 반영)
     */
    public void insertGameRecord(Game game) {
        // [수정사항] admin을 user에 통합했으므로 admin_id 체크 로직은 삭제합니다.
        // 프론트에서 넘어온 user_id가 0(비로그인)인 경우 null로 치환하여 DB 외래키 에러를 방지합니다.
        if (game.getUser_id() != null && game.getUser_id() == 0L) {
            log.info("--- [GameService] 비회원(0) 데이터를 null로 처리하여 저장합니다 ---");
            game.setUser_id(null);
        }

        gameMapper.insertGameRecord(game);
    }
}