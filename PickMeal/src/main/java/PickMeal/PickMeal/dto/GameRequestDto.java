package PickMeal.PickMeal.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameRequestDto {
    private Integer categoryKorean;
    private Integer categoryWestern;
    private Integer categoryChinese;
    private Integer categoryJapanese;
    private Integer categoryAsian;

    private Integer isSpicy;
    private Integer isSoup;
    private Integer isFried;
    private Integer isRoasted;
    private Integer hasPork;
    private Integer hasBeef;
    private List<Integer> askedQuestionIds;
}
