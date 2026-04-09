package com.hankki.pickmeal.service;

import com.hankki.pickmeal.dto.ReviewWishDTO;
import com.hankki.pickmeal.dto.RestaurantDTO;
import com.hankki.pickmeal.mapper.ReviewWishMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewWishMapper reviewWishMapper;

    // 생성자 주입 방식 사용
    public ReviewService(ReviewWishMapper reviewWishMapper) {
        this.reviewWishMapper = reviewWishMapper;
    }

    public List<RestaurantDTO> getPopularRest() {
        return reviewWishMapper.getPopularRest();
    }
    /**
     * 리뷰 정보를 받아서 DB 장부에 기록하는 요리법입니다.
     */
    public void save(ReviewWishDTO dto) {
        // [수정] Mapper에 실제 존재하는 saveInteraction 메서드를 호출합니다.
        reviewWishMapper.saveInteraction(dto);
    }
}
