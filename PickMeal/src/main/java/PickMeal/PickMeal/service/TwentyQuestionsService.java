package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Questions;
import PickMeal.PickMeal.dto.GameRequestDto;
import PickMeal.PickMeal.mapper.FoodMapper;
import PickMeal.PickMeal.mapper.QuestionsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
        return (question != null) ? question : "는 어떠신가요? 🤔";
    }

    public Questions getNextValidQuestion(GameRequestDto request) {

        List<String> unaskedAttributes = new ArrayList<>();

        if (request.getCategoryKorean() == null) unaskedAttributes.add("category_korean");
        if (request.getCategoryWestern() == null) unaskedAttributes.add("category_western");
        if (request.getCategoryChinese() == null) unaskedAttributes.add("category_chinese");
        if (request.getCategoryJapanese() == null) unaskedAttributes.add("category_japanese");
        if (request.getCategoryAsian() == null) unaskedAttributes.add("category_asian");
        if (request.getIsSoup() == null) unaskedAttributes.add("is_soup");
        if (request.getIsSpicy() == null) unaskedAttributes.add("is_spicy");
        if (request.getIsFried() == null) unaskedAttributes.add("is_fried");
        if (request.getIsRoasted() == null) unaskedAttributes.add("is_roasted");
        if (request.getHasPork() == null) unaskedAttributes.add("has_pork");
        if (request.getHasBeef() == null) unaskedAttributes.add("has_beef");
        if (request.getHasNoodle() == null) unaskedAttributes.add("has_noodle");
        if (request.getHasRice() == null) unaskedAttributes.add("has_rice");
        if (request.getHasFlour() == null) unaskedAttributes.add("has_flour");
        if (request.getHasSeafood() == null) unaskedAttributes.add("has_seafood");
        if (request.getHasChicken() == null) unaskedAttributes.add("has_chicken");
        if (request.getIsVeggie() == null) unaskedAttributes.add("is_veggie");
        if (request.getIsCold() == null) unaskedAttributes.add("is_cold");
        if (request.getIsGreasy() == null) unaskedAttributes.add("is_greasy");
        if (request.getIsSweet() == null) unaskedAttributes.add("is_sweet");
        if (request.getIsSolo() == null) unaskedAttributes.add("is_solo");
        if (request.getIsSharing() == null) unaskedAttributes.add("is_sharing");
        if (request.getIsHeavy() == null) unaskedAttributes.add("is_heavy");

        if (unaskedAttributes.isEmpty()) {
            return questionsMapper.getQuestionsByAttributeName("final_recommendation");
        }

        Collections.shuffle(unaskedAttributes);

        String nextAttribute = unaskedAttributes.get(0);
        return questionsMapper.getQuestionsByAttributeName(nextAttribute);
    }


    public String findImagePathByName(String foodName) {
        return foodMapper.findImagePathByName(foodName);
    }

    public List<String> getTopScoredFoods(GameRequestDto request) {
        return foodMapper.getTopScoredFoods(request);
    }
}