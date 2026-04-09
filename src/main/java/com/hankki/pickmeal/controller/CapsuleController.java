package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.Food;
import com.hankki.pickmeal.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/food")
public class CapsuleController {

    @Autowired
    private FoodService foodService;

    /**
     * 카테고리별 랜덤 뽑기 API
     * @param category: 프론트에서 넘어온 카테고리 값 (전체, 한식, 중식 등)
     * defaultValue를 "전체"로 설정하여, 값이 넘어오지 않아도 '전체 뽑기'가 되도록 했습니다.
     */
    @GetMapping("/draw")
    public Food drawCapsule(@RequestParam(value = "category", defaultValue = "전체") String category) {

        // 1. 로그를 남겨서 프론트에서 어떤 값이 넘어오는지 확인하면 디버깅이 편합니다.
        System.out.println("뽑기 요청된 카테고리: " + category);

        // 2. 서비스 단으로 처리를 넘깁니다.
        // 서비스 내부에 "전체"일 때와 "특정 카테고리"일 때를 구분하는 로직이 들어가야 합니다.
        return foodService.drawCapsuleMenu(category);
    }
}