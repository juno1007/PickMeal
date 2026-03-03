package PickMeal.PickMeal.dto;

import lombok.Data;

@Data
public class FoodDTO {
    private Long foodId;      // 음식 고유 번호 (PK)
    private String foodName;  // 음식 이름
    private String category;  // 음식 카테고리 (중식, 일식 등)
    private String description; // 음식 설명
    private String imagePath;  // 이미지 경로
    private int winCount;     // 월드컵 우승 횟수
}
