package com.hankki.pickmeal.service;

import com.hankki.pickmeal.dto.PlaceStatsDto;
import com.hankki.pickmeal.dto.RestaurantDTO;
import com.hankki.pickmeal.mapper.RestaurantMapper;
import com.hankki.pickmeal.mapper.ReviewWishMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantMapper restaurantMapper;
    private final ReviewWishMapper wishMapper;

    public List<RestaurantDTO> findAll() {
        return restaurantMapper.findAll();
    }

    @Transactional
    // [수정] Long userPk를 String userId로 변경합니다.
    // 우리 프로젝트는 이제 'woals106' 같은 문자열 아이디를 사용하기 때문입니다.
    public void saveWish(Long restId, String userId) {
        // 매퍼 인터페이스도 String을 받도록 고쳤으므로 이제 에러가 사라집니다.
        wishMapper.insertWish(restId, userId);
    }

    /**
     * 사용자가 찜한 식당 리스트 가져오기
     * @param userId (aaa, kakao_12345 등)
     * @return 찜한 식당 통계 정보 리스트
     */
    public List<PlaceStatsDto> getMyLikedResturants(String userId) {
        // 매퍼를 호출하여 DB에서 데이터를 가져옵니다.
        List<PlaceStatsDto> likedList = restaurantMapper.getMyLikedResturants(userId);

        // 데이터 가공이 필요하다면 여기서 수행합니다. (예: 주소 포맷팅 등)
        // 현재는 DTO에 맞춰 그대로 반환합니다.
        return likedList;
    }

    @Transactional
    public void addViewLog(String kakaoPlaceId, String userId) {
        // 1. 조회수 컬럼 직접 증가 (위에서 만든 매퍼 호출)
        restaurantMapper.updatePlaceStatsView(kakaoPlaceId);
    }
}