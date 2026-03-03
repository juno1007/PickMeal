package PickMeal.PickMeal.controller;

import PickMeal.PickMeal.dto.ReviewWishDTO;
import PickMeal.PickMeal.dto.RestaurantDTO;
import PickMeal.PickMeal.mapper.RestaurantMapper;
import PickMeal.PickMeal.mapper.ReviewWishMapper;
import PickMeal.PickMeal.service.RestaurantService;
import PickMeal.PickMeal.service.ReviewService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class RestaurantController {

    private final RestaurantMapper restaurantMapper;
    private final ReviewWishMapper reviewWishMapper;
    private final RestaurantService restaurantService;
    private final ReviewService reviewService;

    public RestaurantController(RestaurantMapper restaurantMapper,
                                ReviewWishMapper reviewWishMapper,
                                RestaurantService restaurantService,
                                ReviewService reviewService) {
        this.restaurantMapper = restaurantMapper;
        this.reviewWishMapper = reviewWishMapper;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    /**
     * [수정] 문자열 아이디('aaa')가 아닌, 유저의 '숫자 고유번호(PK)'를 가져오는 메서드입니다.
     * 사용자님의 CustomUserDetails 구조에 따라 내부 로직(getUserNo 등)은 다를 수 있습니다.
     */
    private Long getLoginUserPk(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        // 시큐리티에서 유저 객체를 꺼내 유저의 숫자 번호를 가져옵니다.
        // 만약 이 부분이 어렵다면 우선 임시로 DB에 있는 유저의 번호(예: 1L)를 리턴해서 테스트해보세요.
        try {
            // 예시: CustomUserDetails 객체에서 PK를 꺼내는 방식
            // return ((CustomUserDetails) authentication.getPrincipal()).getUserNo();
            return 1L; // ★ 임시 테스트용: 실제 DB에 있는 유저 번호 숫자를 넣어보세요.
        } catch (Exception e) {
            return null;
        }
    }

    // 1. 맛집 탐지기 페이지 접속
    @GetMapping("/meal-spotter")
    public String mealSpotter(Model model, Authentication authentication) {
        List<RestaurantDTO> restaurants = restaurantService.findAll();

        // [수정] 문자열 대신 숫자 PK를 가져옵니다.
        Long userPk = getLoginUserPk(authentication);
        System.out.println("현재 로그인 유저 PK: " + userPk);

        for (RestaurantDTO res : restaurants) {
            // 리뷰 개수 세팅
            int reviewCount = reviewWishMapper.getReviewCount(res.getRestId());
            res.setReviewCount(reviewCount);

            // 찜 개수 세팅
            int wishCount = reviewWishMapper.getTotalWishCount(res.getRestId());
            res.setWishCount(wishCount);

            // [수정] 찜 상태 확인 시 '숫자 PK'를 전달합니다.
            if (userPk != null) {
                int check = reviewWishMapper.checkWish(userPk, res.getRestId());
                res.setWished(check > 0);
            } else {
                res.setWished(false);
            }
        }

        model.addAttribute("restaurantList", restaurants);
        return "board/meal-spotter";
    }

    // 3. 리뷰 저장 API
    @PostMapping("/api/review/save")
    @ResponseBody
    public int saveReview(@RequestBody ReviewWishDTO dto, Authentication auth) {
        Long userPk = getLoginUserPk(auth);

        if (userPk != null) {
            // [수정] DTO에 문자열 대신 숫자 PK를 담아줍니다.
            // DTO의 userId 필드 타입도 Long으로 바뀌어 있어야 합니다!
            dto.setUserId(userPk);
        }

        reviewService.save(dto);
        return reviewWishMapper.getReviewCount(dto.getResId());
    }

    // RestaurantController.java 의 toggleWish 메서드 부분 수정
    @PostMapping("/api/wishlist/{resId}")
    @ResponseBody
    public String toggleWish(@PathVariable Long resId, Authentication authentication) {
        // 1. [수정] 테스트를 위해 임시로 '1L'이라는 숫자 PK를 사용합니다.
        // DB의 user 테이블에 있는 'aaa' 유저의 실제 번호가 1번이라면 1L을 넣습니다.
        Long userPk = 1L;

        // 2. 로그인이 안 된 경우 처리
        if (userPk == null) return "fail";

        // 3. [수정] 이제 'aaa'가 아닌 숫자 1L이 전달되므로 데이터 타입 에러가 사라집니다.
        int alreadyWished = reviewWishMapper.checkWish(userPk, resId);

        if (alreadyWished > 0) {
            // 이미 찜 상태라면 삭제(취소) 처리
            reviewWishMapper.deleteWish(userPk, resId);
        } else {
            // 찜 상태가 아니라면 새로 저장
            reviewWishMapper.insertWish(resId, userPk);
        }

        // 4. 최신 찜 개수를 반환합니다.
        return String.valueOf(reviewWishMapper.getTotalWishCount(resId));
    }
    @GetMapping("/api/reviews/{resId}") // ★ 이 주소가 정확한지 확인!
    @ResponseBody // ★ 이 어노테이션이 꼭 있어야 합니다.
    public List<ReviewWishDTO> getReviewList(@PathVariable Long resId) {
        return reviewWishMapper.getReviewsByRestaurant(resId);


    }

    @GetMapping("/restaurant/detail/{restId}")
    public String getRestaurantDetail(@PathVariable Long restId){

        //RestaurantDTO restaurant = restaurantService.getRestaurantDetail(restId);



        return "";
    }

}