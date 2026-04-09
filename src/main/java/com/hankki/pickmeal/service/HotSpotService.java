package com.hankki.pickmeal.service;

import com.hankki.pickmeal.domain.HotSpot;
import com.hankki.pickmeal.dto.PlaceStatsDto;
import com.hankki.pickmeal.mapper.HotSpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotSpotService {

    private final HotSpotMapper hotSpotMapper;
    private final PlaceStatsService placeStatsService;

    public void addHotspot(String kakaoPlaceId) {
        PlaceStatsDto placeStatsDto = placeStatsService.getPlaceStatByKakaoIds(kakaoPlaceId);

        long res_id = Long.parseLong(placeStatsDto.getKakaoPlaceId());

        hotSpotMapper.addHotspot(res_id);
    }

    public void deleteHotspot(String kakaoPlaceId) {
        PlaceStatsDto placeStatsDto = placeStatsService.getPlaceStatByKakaoIds(kakaoPlaceId);

        long res_id = Long.parseLong(placeStatsDto.getKakaoPlaceId());

        hotSpotMapper.deleteHotspot(res_id);
    }

    public List<HotSpot> getHotSpotList() {
        return hotSpotMapper.getHotSpotList();
    }
}
