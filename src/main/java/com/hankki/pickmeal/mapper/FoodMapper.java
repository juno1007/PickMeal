package com.hankki.pickmeal.mapper;

import com.hankki.pickmeal.dto.GameRequestDto;
import com.hankki.pickmeal.domain.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FoodMapper {

    List<String> getFilteredFoods(GameRequestDto request);
    // XML의 id="getRandomFoodByCategory"와 이름을 맞춥니다.
    // @Param("category")를 통해 XML의 #{category}에 값이 전달됩니다.
    Food getRandomFoodByCategory(@Param("category") String category);

    String findImagePathByName(String foodName);

    Food findByName(@Param("name") String name);

    List<Food> getWinnerFoodList();

    List<String> getTopScoredFoods(GameRequestDto request);
}