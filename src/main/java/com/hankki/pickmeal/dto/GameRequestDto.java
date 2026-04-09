package com.hankki.pickmeal.dto;

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
    private Integer hasNoodle;
    private Integer hasRice;
    private Integer hasFlour;
    private Integer hasSeafood;
    private Integer hasChicken;
    private Integer isVeggie;
    private Integer isCold;
    private Integer isGreasy;
    private Integer isSweet;
    private Integer isSolo;
    private Integer isSharing;
    private Integer isHeavy;
    private List<Integer> askedQuestionIds;
}
