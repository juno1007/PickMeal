package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.HotSpot;
import com.hankki.pickmeal.dto.PlaceStatsDto;
import com.hankki.pickmeal.dto.ReviewWishDTO;
import com.hankki.pickmeal.mapper.RestaurantMapper;
import com.hankki.pickmeal.mapper.ReviewWishMapper;
import com.hankki.pickmeal.service.HotSpotService;
import com.hankki.pickmeal.service.PlaceStatsService;
import com.hankki.pickmeal.service.RestaurantService;
import com.hankki.pickmeal.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor // [수정] 직접 작성했던 생성자를 지우고, 이 어노테이션으로 스프링 빈 자동 주입을 처리합니다.
public class RestaurantController {

    private final RestaurantMapper restaurantMapper;
    private final ReviewWishMapper reviewWishMapper;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;
    private final PlaceStatsService placeStatsService;
    private final HotSpotService hotSpotService; // [추가] @RequiredArgsConstructor 덕분에 자동으로 주입됩니다.

    // 로그인 유저의 문자열 ID(예: woals106)를 가져오는 메서드
    private String getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return authentication.getName();
    }

    // 1. 맛집 탐지기 페이지 접속
    // 1. 맛집 탐지기 페이지 접속
    @GetMapping("/meal-spotter")
    public String mealSpotter(Model model, Authentication authentication) {
        String userId = getLoginUserId(authentication);

        // 1. DB의 hot_spot 테이블에서 핫스팟 목록 가져오기
        List<HotSpot> hotSpotList = hotSpotService.getHotSpotList();
        List<PlaceStatsDto> popularPlaceList = new ArrayList<>();

        // [핵심] 이 줄이 빠져서 빨간 줄이 떴던 겁니다! 중복 검사용 바구니(Set) 생성
        java.util.Set<Long> uniqueResIds = new java.util.HashSet<>();

        // 2. 각 핫스팟의 상세 통계 정보 불러오기
        for (HotSpot hs : hotSpotList) {
            Long resId = hs.getResId();

            // 3. 중복 검사: 이미 바구니에 있는 resId면 이번 턴은 건너뜀
            if (uniqueResIds.contains(resId)) {
                continue;
            }
            // 처음 보는 resId면 바구니에 담아서 기록해둠
            uniqueResIds.add(resId);

            // 통계 정보 가져오기
            PlaceStatsDto dto = placeStatsService.getPlaceStatByKakaoIds(String.valueOf(resId));

            if (dto != null) {
                if (userId != null) {
                    // 유저가 로그인한 상태라면 찜(좋아요) 여부 체크
                    int check = reviewWishMapper.checkWish(userId, resId);
                    dto.setLiked(check > 0);
                }
                popularPlaceList.add(dto);
            }
        }

        popularPlaceList.sort((a, b) -> Integer.compare(b.getHeartCount(), a.getHeartCount()));

        // 중복이 제거된 핫스팟 데이터를 화면으로 전달
        model.addAttribute("popularPlaceList", popularPlaceList);
        return "board/meal-spotter";
    }

    // 3. 리뷰 저장 API
    @PostMapping("/api/review/save")
    @ResponseBody
    public int saveReview(@RequestBody ReviewWishDTO dto, Authentication auth) {
        String userId = getLoginUserId(auth);

        if (userId != null) {
            dto.setUserId(userId);
        }

        // 1. 리뷰 저장 실행
        reviewService.save(dto);

        try {
            // String인 resId를 숫자로 변환하여 매퍼에 전달
            Long resIdLong = Long.parseLong(dto.getResId());
            return reviewWishMapper.getReviewCount(resIdLong);
        } catch (Exception e) {
            System.out.println("리뷰 개수 조회 중 에러 발생: " + e.getMessage());
            return 0; // 에러 시 기본값 반환
        }
    }

    // 찜하기 토글 API
    @PostMapping("/api/wishlist/{resId}")
    @ResponseBody
    public String toggleWish(@PathVariable Long resId, Authentication authentication) {
        String userId = getLoginUserId(authentication);

        if (userId == null) return "fail";

        // 문자열 ID를 그대로 사용하여 찜 상태 확인
        int alreadyWished = reviewWishMapper.checkWish(userId, resId);

        if (alreadyWished > 0) {
            reviewWishMapper.deleteWish(userId, resId);
        } else {
            reviewWishMapper.insertWish(resId, userId);
        }

        return String.valueOf(reviewWishMapper.getTotalWishCount(resId));
    }

    @GetMapping("/api/reviews/{resId}")
    @ResponseBody
    public List<ReviewWishDTO> getReviewList(@PathVariable Long resId) {
        return reviewWishMapper.getReviewsByRestaurant(resId);
    }

    @DeleteMapping("/api/review/delete/{reviewId}")
    @ResponseBody
    public String deleteReview(@PathVariable Long reviewId) {
        reviewWishMapper.deleteReview(reviewId);
        return "success";
    }
}