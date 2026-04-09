package com.hankki.pickmeal.dto;

import lombok.Data;

@Data
public class ReviewWishDTO {
    private Long reviewId;
    private String resId;
    private String userId;

    private Integer rating;    // 별점
    private String content;    // 리뷰 내용
    private boolean isWish;    // 찜 여부 (true/false)
    private String createdAt;
}