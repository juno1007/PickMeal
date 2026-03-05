package PickMeal.PickMeal.controller;

import PickMeal.PickMeal.domain.Questions;
import PickMeal.PickMeal.dto.GameRequestDto;
import PickMeal.PickMeal.dto.GameResponseDto;
import PickMeal.PickMeal.service.TwentyQuestionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/twenty-questions")
@RequiredArgsConstructor
public class TwentyQuestionsController {

    private final TwentyQuestionsService twentyQuestionsService;


    @PostMapping("/next")
    public ResponseEntity<GameResponseDto> getNextStep(@RequestBody GameRequestDto request) {
        GameResponseDto response = new GameResponseDto();

        int askedCount = request.getAskedQuestionIds() != null ? request.getAskedQuestionIds().size() : 0;

        Questions nextQuestion = twentyQuestionsService.getNextValidQuestion(request);

        if (askedCount >= 15 || nextQuestion == null) {
            response.setStatus("FINAL_CHOICE");
            response.setRemain_foodList(twentyQuestionsService.getTopScoredFoods(request));
            return ResponseEntity.ok(response);
        }

        response.setStatus("QUESTION");
        response.setNextQuestion_id(nextQuestion.getQuestion_id());
        response.setNextQuestion_text(nextQuestion.getQuestion_text());
        response.setNextAttribute_name(nextQuestion.getAttribute_name());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/food/imagePath")
    public String getImagePath(@RequestParam("foodName") String foodName) {
        String imagePath = twentyQuestionsService.findImagePathByName(foodName);
        return imagePath;
    }
}