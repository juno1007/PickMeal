package PickMeal.PickMeal.controller;

import PickMeal.PickMeal.domain.Food;
import PickMeal.PickMeal.domain.Game;
import PickMeal.PickMeal.domain.HotSpot;
import PickMeal.PickMeal.domain.User;
import PickMeal.PickMeal.service.FoodService;
import PickMeal.PickMeal.service.GameService;
import PickMeal.PickMeal.service.UserService;
import PickMeal.PickMeal.service.RestaurantService;
import lombok.extern.slf4j.Slf4j;
import PickMeal.PickMeal.dto.PlaceStatsDto;
import PickMeal.PickMeal.service.*;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private final HotSpotService hotSpotService;
    private final PlaceStatsService placeStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private RestaurantService restaurantService; // 추가

    @Autowired
    private FoodService foodService;

    @Autowired
    private GameService gameService;

    @GetMapping("/")
    public String index() {
        return "index"; // 인트로 화면
    }

    @GetMapping("/next-page")
    public String next(Model model) {
        // 1. 기존처럼 DB에서 핫스팟 목록 전체 가져오기
        List<HotSpot> hotSpotList = hotSpotService.getHotSpotList();

        // 2. 중복을 걸러낸 식당만 담을 '새 리스트'와 중복 검사용 'Set' 생성
        List<HotSpot> uniqueHotSpotList = new java.util.ArrayList<>();
        java.util.Set<Long> uniqueResIds = new java.util.HashSet<>();

        for(HotSpot hs : hotSpotList){
            Long resId = hs.getResId();

            // 3. 중복 검사: 이미 Set에 들어있는 가게 식별자면 아래 코드를 무시하고 다음 반복으로 넘어감
            if (uniqueResIds.contains(resId)) {
                continue;
            }
            // 처음 확인된 가게면 Set에 기록해둠
            uniqueResIds.add(resId);

            // 4. 기존 로직 유지 (통계 정보 세팅)
            PlaceStatsDto placeStatsDto = placeStatsService.getPlaceStatByKakaoIds(String.valueOf(resId));

            // 정보가 정상적으로 있는 경우에만 세팅 후 새 리스트에 추가
            if (placeStatsDto != null) {
                hs.setAddress(placeStatsDto.getAddress());
                hs.setCategory(placeStatsDto.getCategory());
                hs.setPlaceName(placeStatsDto.getPlaceName());
                hs.setWishCount(placeStatsDto.getHeartCount());
                hs.setViewCount(placeStatsDto.getViewCount());
                hs.setReviewCount(placeStatsDto.getReviewCount());
                hs.setAvgRating(placeStatsDto.getAvgRating());

                uniqueHotSpotList.add(hs); // 중복이 아닌 고유 식당만 추가
            }
        }
        uniqueHotSpotList.sort((a, b) -> Integer.compare(b.getWishCount(), a.getWishCount()));

        List<Food> gameTopList = foodService.getWinnerFoodList();

        model.addAttribute("gameTopList", gameTopList);

        // 5. 중복이 제거된 새 리스트(uniqueHotSpotList)를 화면으로 전달하도록 수정
        model.addAttribute("hotSpotList", uniqueHotSpotList);
        return "next-page";
    }


    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal User user, Model model) {
        // 세션에 저장된 현재 로그인 유저 정보(@AuthenticationPrincipal)를 모델에 담습니다.
        // 만약 User 객체가 null이면 로그인 페이지로 보내거나 예외 처리를 해야 합니다.
        if (user == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("user", user); // 'user'라는 이름으로 객체를 넘겨줌
        return "users/mypage";
    }

    // 룰렛 돌리기 페이지
    @GetMapping("/roulette")
    public String roulettePage() {
        return "game/roulette";
    }

    @GetMapping("/twentyQuestions")
    public String twentyQuestionsPage() {
        return "game/twentyQuestions"; // templates/twentyQuestions.html 반환
    }

    @GetMapping("/capsule")
    public String goCapsulePage() {
        return "game/capsule";
    }

    @GetMapping("/users/forgot-pw")
    public String forgotPwPage() {
        return "users/forgot-pw";
    }

    @GetMapping("/board")
    public String boardPage() {
        return "board/board"; // templates/board.html 파일을 반환
    }

    @GetMapping("/game")
    public String gamePage() {
        return "game/game";
    }

    @GetMapping("/worldcup/setup") // 이 주소로 들어오면
    public String gameSetupPage() {
        return "game/game_setup"; // game_setup.html 파일을 보여줍니다.
    }

    @GetMapping("/worldcup")
    public String worldcupRedirect() {
        // [비유] 손님이 /worldcup으로 들어오면, "설정 화면으로 모실게요~" 하고 방향을 돌려주는(redirect) 역할입니다.
        return "redirect:/worldcup/setup";
    }

    @GetMapping("/game/play")
    public String playWorldCup(
            @RequestParam(value = "types") List<String> types,
            @RequestParam(value = "round") int round,
            @AuthenticationPrincipal User user,
            Authentication authentication,
            Model model) {

        // 1. [핵심] 133개의 전체 음식을 넉넉하게 가져옵니다.
        // round(8)를 넣지 말고, 전체 개수보다 큰 숫자(예: 200)를 넣어서 다 가져오게 합니다.
        List<Food> allAvailableFoods = userService.getMixedFoods(types, 200);

        // 2. 로그인 유저 ID 찾기 (ID 조합 규칙 통일!)
        Long userId = null;
        if (user != null && user.getUser_id() != null) {
            userId = user.getUser_id();
        }else if (authentication != null && authentication.isAuthenticated()) {
            String finalId = "";

            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
                var attributes = token.getPrincipal().getAttributes();
                String registrationId = token.getAuthorizedClientRegistrationId(); // "google", "naver" 등
                String oauthName = token.getName(); // 구글의 경우 보통 "google_116..." 형태

                if ("naver".equals(registrationId)) {
                    java.util.Map<String, Object> response = (java.util.Map<String, Object>) attributes.get("response");
                    finalId = registrationId + "_" + response.get("id");
                } else {
                    // [핵심 수정] oauthName이 이미 registrationId로 시작하는지 확인합니다.
                    // google_116... 이 이미 google_ 로 시작한다면 또 붙이지 않습니다.
                    if (oauthName.startsWith(registrationId + "_")) {
                        finalId = oauthName;
                    } else {
                        finalId = registrationId + "_" + oauthName;
                    }
                }
            } else {
                finalId = authentication.getName();
            }

            log.info("--- [수정후] 최종 DB 조회 시도 ID: {}", finalId);

            User dbUser = userService.findById(finalId);
            if (dbUser != null) {
                userId = dbUser.getUser_id();
            }
        }

        log.info("--- [최종 확인] 찾아낸 유저 숫자 ID: {}", userId);

        // 3. 이제 '숫자 ID'가 잘 전달되면 '날것' 필터링이 작동합니다!
        List<Food> filteredFoods = gameService.getPriorityFoodList(userId, allAvailableFoods, round);

        // 4. 화면으로 전달
        model.addAttribute("foods", filteredFoods);
        model.addAttribute("totalRound", round);

        log.info("--- [최종 결과] 133개 중 필터링 완료. 화면에 {}개의 음식을 보냅니다.", filteredFoods.size());

        return "game/worldcup";
    }

    @GetMapping("/api/food/image")
    @ResponseBody
    public String getFoodImage(@RequestParam("name") String name) {
        Food food = foodService.findFoodByName(name);

        // DB의 imagePath에 이미 "/images/Korean food/..." 가 들어있으므로 그대로 반환합니다.
        if (food != null && food.getImagePath() != null) {
            return food.getImagePath().trim();
        }

        return "/images/meal.png";
    }

    // MainController.java 에 추가
    @GetMapping("/worldcup/ranking")
    public String rankingPage(Model model) {
        // 1. 서비스(일꾼)에게 DB에서 인기 음식 10개를 가져오라고 시킵니다.
        // (이미 UserService에 getTop10Foods를 만드셨다면 바로 사용!)
        List<Food> rankingList = userService.getTop10Foods();

        // 2. 가져온 '진짜 데이터'를 'rankingList'라는 이름으로 접시에 담습니다.
        model.addAttribute("rankingList", rankingList);

        // 3. 데이터가 담긴 접시를 들고 ranking.html로 이동합니다.
        return "game/ranking";
    }

    @PostMapping("/worldcup/win/{foodId}")
    @ResponseBody
    public String updateWinCount(@PathVariable("foodId") Long foodId,
                                 @RequestParam(value="gameType", defaultValue="worldcup") String gameType,
                                 Authentication authentication) {
        try {
            // 1. 음식 전체 승리 카운트 증가
            userService.updateFoodWinCount(foodId);

            // 2. Game 객체 생성 및 기본 정보 설정
            Game game = new Game();
            game.setFood_id(foodId);
            game.setGameType(gameType);
            game.setPlayDate(LocalDateTime.now());
            game.setUser_id(null); // 기본값은 비로그인(null)

            // 3. 로그인 사용자 정보 처리 (통합 버전)
            if (authentication != null && authentication.isAuthenticated()) {
                // 시큐리티에서 현재 로그인한 ID 추출
                String loginName = authentication.getName();

                // 소셜 로그인 여부 확인 및 ID 조합 (기존 UserService 로직 활용)
                String registrationId = "";
                if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token) {
                    registrationId = token.getAuthorizedClientRegistrationId();
                }

                // 통합된 ID 규칙 적용 (일반/관리자는 loginName 그대로, 소셜은 prefix_loginName)
                String fullUserId = (registrationId == null || registrationId.isEmpty() || loginName.startsWith(registrationId))
                        ? loginName : registrationId + "_" + loginName;

                // 통합된 user 테이블에서 사용자 조회
                User user = userService.findById(fullUserId);

                if (user != null) {
                    // 관리자든 일반 유저든 찾은 User 객체의 ID를 game의 user_id에 저장
                    game.setUser_id(user.getUser_id());
                    System.out.println("게임 기록 저장 대상 ID: " + user.getUser_id() + " (권한: " + user.getRole() + ")");
                }
            }

            // 4. 게임 기록 저장 (로그인 여부와 상관없이 실행)
            gameService.insertGameRecord(game);
            return "success";

        } catch (Exception e) {
            log.error("월드컵 기록 저장 중 에러 발생: ", e);
            return "fail";
        }
    }

    @GetMapping("/api/food/getIdByName")
    @ResponseBody
    public Long getFoodIdByName(@RequestParam("foodName") String foodName) {
        Food food = foodService.findFoodByName(foodName);

        if (food != null) {
            return food.getFoodId(); // 음식의 PK(숫자 ID) 반환
        }
        return null; // 찾지 못했을 경우
    }

}



