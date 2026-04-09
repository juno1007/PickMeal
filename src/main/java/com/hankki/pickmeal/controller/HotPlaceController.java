package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.dto.PlaceStatsDto;
import com.hankki.pickmeal.service.HotSpotService;
import com.hankki.pickmeal.service.PlaceStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/hotplace")
@RequiredArgsConstructor
public class HotPlaceController {

    private final PlaceStatsService placeStatsService;
    private final HotSpotService hotSpotService;

    @GetMapping("")
    public String hotPlacePage() {
        return "hotplace";
    }

    @PostMapping("/stats")
    @ResponseBody
    public List<PlaceStatsDto> getStats(@RequestBody List<String> placeIds, Principal principal) {
        // 로그인 상태면 ID 전달, 아니면 null 전달
        String userId = (principal != null) ? principal.getName() : null;
        return placeStatsService.getStatsByKakaoIds(placeIds, userId);
    }

    @PostMapping("/view/{kakaoPlaceId}")
    @ResponseBody
    public ResponseEntity<?> addView(@PathVariable("kakaoPlaceId") String kakaoPlaceId, Principal principal) {
        String userId = (principal != null) ? principal.getName() : "guest";
        // 이 서비스 호출이 위에서 만든 updatePlaceStatsView까지 이어져야 합니다!
        placeStatsService.addViewLog(kakaoPlaceId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/heart/{kakaoPlaceId}")
    @ResponseBody
    public ResponseEntity<?> toggleHeart(@PathVariable("kakaoPlaceId") String kakaoPlaceId, Principal principal, @RequestParam(value = "placeName", required = false) String placeName,
                                         @RequestParam(value = "categoryName", required = false) String categoryName,
                                         @RequestParam(value = "addressName", required = false) String addressName) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        boolean isLiked = placeStatsService.toggleHeart(kakaoPlaceId, principal.getName(), placeName, categoryName, addressName);

        if(isLiked){
            if(placeStatsService.countHeart(kakaoPlaceId) == 10){
                hotSpotService.addHotspot(kakaoPlaceId);
            }
        }else{
            if(placeStatsService.countHeart(kakaoPlaceId) == 9){
                hotSpotService.deleteHotspot(kakaoPlaceId);
            }
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/reviews/{kakaoPlaceId}")
    @ResponseBody
    public List<PlaceStatsDto> getReviews(@PathVariable String kakaoPlaceId) {
        return placeStatsService.getReviews(kakaoPlaceId);
    }

    @PostMapping("/reviews/{kakaoPlaceId}")
    @ResponseBody
    public ResponseEntity<?> addReview(@PathVariable String kakaoPlaceId, @RequestBody java.util.Map<String, Object> data, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // JSON으로 넘어온 데이터 분리
        String content = (String) data.get("content");
        int rating = Integer.parseInt(data.get("rating").toString());
        String placeName = (String) data.get("placeName");
        String category = (String) data.get("category");
        String address = (String) data.get("address");


        placeStatsService.addReview(kakaoPlaceId, principal.getName(), content, rating, placeName, category, address);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/update/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId, @RequestBody java.util.Map<String, Object> data, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 1. null일 경우 빈 문자열이나 기본값 처리
        String content = data.get("content") != null ? data.get("content").toString() : "";

        // 2. rating 처리 (가장 위험한 부분)
        int rating = 0;
        if (data.get("rating") != null) {
            rating = Integer.parseInt(data.get("rating").toString());
        }

        // 3. placeName 처리 (String.valueOf는 null이 들어오면 "null"이라는 문자열을 반환해서 안전함)
        String placeName = String.valueOf(data.get("placeName"));

        String userId = principal.getName();

        boolean isUpdated = placeStatsService.updateReview(reviewId, userId, content, rating, placeName);

        if (!isUpdated) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reviews/delete/{reviewId}")
    @ResponseBody
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 서비스의 deleteReview 호출
        placeStatsService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }
}