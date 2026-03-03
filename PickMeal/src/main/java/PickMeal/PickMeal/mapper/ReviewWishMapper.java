package PickMeal.PickMeal.mapper;

import PickMeal.PickMeal.dto.RestaurantDTO;
import PickMeal.PickMeal.dto.ReviewWishDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // ★ Param 임포트가 꼭 있어야 합니다.
import java.util.List;

import java.util.List;

@Mapper
public interface ReviewWishMapper {
    // 1. 기존에 있던 찜/리뷰 저장 쿼리
    void saveInteraction(ReviewWishDTO dto);

    // 2. 기존에 있던 찜 상태 가져오기
    Integer getWishStatus(ReviewWishDTO dto);

    List<RestaurantDTO> getPopularRest();

    // ★ 3. 아래 코드를 새로 추가하세요! (Service 에러 해결사)
    void insertWish(@Param("restId") Long restId, @Param("userId") Long userId);

    int getTotalWishCount(Long resId);

    int checkWish(@Param("userId") Long userId, @Param("resId") Long resId);

    void deleteWish(@Param("userId") Long userId, @Param("resId") Long resId);

    void insertReview(ReviewWishDTO dto);

    int getReviewCount(@Param("resId") Long resId);

    List<ReviewWishDTO> getReviewsByRestaurant(@Param("resId") Long resId);

    void deleteReview(Long reviewId);
}