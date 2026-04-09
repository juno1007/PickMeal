package com.hankki.pickmeal.mapper;

import com.hankki.pickmeal.domain.HotSpot;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotSpotMapper {
    void addHotspot(long res_id);

    void deleteHotspot(long resId);

    List<HotSpot> getHotSpotList();
}
