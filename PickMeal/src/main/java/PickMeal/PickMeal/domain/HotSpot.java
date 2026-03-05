package PickMeal.PickMeal.domain;

import lombok.Data;

import java.util.Date;

@Data
public class HotSpot {
    private Long hotspotId;
    private Long reviewId;
    private Long resId;
    private Date createdAt;
    private int wishCount;
    private String address;
    private String category;
    private String placeName;
    private int viewCount;
    private int reviewCount;
    private double avgRating;
    private boolean isWished;
}
