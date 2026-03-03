package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Questions;
import PickMeal.PickMeal.dto.GameRequestDto;
import PickMeal.PickMeal.mapper.FoodMapper;
import PickMeal.PickMeal.mapper.QuestionsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TwentyQuestionsService {
    private final QuestionsMapper questionsMapper;
    private final FoodMapper foodMapper;

    public List<String> getFilteredFoods(GameRequestDto request) {
        return foodMapper.getFilteredFoods(request);
    }

    public String getFinalQuestion() {
        String question = questionsMapper.getFinalQuestion();
        // [수정] DB에서 가져온 값이 null이면 기본 문구를 돌려줍니다.
        return (question != null) ? question : "는 어떠신가요? 🤔";
    }

    public Questions getNextValidQuestion(GameRequestDto request) {
        // 1. 카테고리 질문 단계
        // 하나라도 '네(1)'라고 답한 카테고리가 있는지 확인합니다.
        boolean hasCategory = isCategorySelected(request);

        if (!hasCategory) {
            // 아직 아무 카테고리도 선택하지 않았다면, 순차적으로 물어봅니다.
            if (request.getCategoryKorean() == null) return questionsMapper.getQuestionsByAttributeName("category_korean");
            if (request.getCategoryWestern() == null) return questionsMapper.getQuestionsByAttributeName("category_western");
            if (request.getCategoryChinese() == null) return questionsMapper.getQuestionsByAttributeName("category_chinese");
            if (request.getCategoryJapanese() == null) return questionsMapper.getQuestionsByAttributeName("category_japanese");
            if (request.getCategoryAsian() == null) return questionsMapper.getQuestionsByAttributeName("category_asian");
        }

        // 2. 카테고리가 정해졌거나(1), 모든 카테고리에 '아니요(0)'를 했다면 세부 질문으로 진입
        return getNextAttributeQuestion(request);
    }

    // 카테고리 선택 여부를 확인하는 도우미 메서드
    private boolean isCategorySelected(GameRequestDto request) {
        return (request.getCategoryKorean() != null && request.getCategoryKorean() == 1) ||
                (request.getCategoryWestern() != null && request.getCategoryWestern() == 1) ||
                (request.getCategoryChinese() != null && request.getCategoryChinese() == 1) ||
                (request.getCategoryJapanese() != null && request.getCategoryJapanese() == 1) ||
                (request.getCategoryAsian() != null && request.getCategoryAsian() == 1);
    }

    private Questions getNextAttributeQuestion(GameRequestDto request) {
        // 여기서부터는 질문 순서대로 쭉쭉 진행됩니다.
        if (request.getIsSoup() == null) return questionsMapper.getQuestionsByAttributeName("is_soup");
        if (request.getIsSpicy() == null) return questionsMapper.getQuestionsByAttributeName("is_spicy");
        if (request.getIsFried() == null) return questionsMapper.getQuestionsByAttributeName("is_fried");
        if (request.getIsRoasted() == null) return questionsMapper.getQuestionsByAttributeName("is_roasted");
        if (request.getHasPork() == null) return questionsMapper.getQuestionsByAttributeName("has_pork");
        if (request.getHasBeef() == null) return questionsMapper.getQuestionsByAttributeName("has_beef");

        return questionsMapper.getQuestionsByAttributeName("final_recommendation");
    }

    public String findImagePathByName(String foodName) {
        return foodMapper.findImagePathByName(foodName);
    }
}