package com.hankki.pickmeal.domain;

import lombok.Data;

@Data
public class Food {
    private Long foodId;
    private String foodName;
    private String category;
    private String description;
    private String imagePath;
    private int winCount;     // 우승 횟수
}