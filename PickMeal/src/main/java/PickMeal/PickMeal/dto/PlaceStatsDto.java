package PickMeal.PickMeal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceStatsDto {
    private Long reviewId;
    private String kakaoPlaceId;
    private int viewCount;
    private int heartCount;
    private int reviewCount;
    private String userId;
    private String content;
    private boolean liked;
    private int rating;
    private double avgRating;
    private String placeName;
    private String nickname;
}