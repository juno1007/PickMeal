package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.dto.RestaurantDTO;
import com.hankki.pickmeal.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller // 이 클래스가 브라우저의 요청을 받는 안내원임을 선언합니다.
public class MapController {

    @Autowired // 식당 정보를 관리하는 서비스(요리사)를 자동으로 연결합니다.
    private RestaurantService restaurantService;

    // MapController.java 파일 수정
    @GetMapping("/map-test") // 기존 "/meal-spotter"에서 "/map-test"로 주소를 바꿨습니다!
    public String myHotPlace(Model model) {
        // 1. 서비스에게 "인기 식당 리스트 다 가져와"라고 시킵니다.
        List<RestaurantDTO> restaurantList = restaurantService.findAll();

        // 2. 'restaurants'라는 바구니에 데이터를 담아 HTML 화면으로 보냅니다.
        model.addAttribute("restaurants", restaurantList);

        // 3. board 폴더 안에 있는 meal-spotter.html 파일을 보여주라는 뜻입니다.
        return "board/meal-spotter";
    }
}