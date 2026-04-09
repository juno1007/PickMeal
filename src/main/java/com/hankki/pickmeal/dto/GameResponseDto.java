package com.hankki.pickmeal.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GameResponseDto {
    private String status;
    private long nextQuestion_id;
    private String nextQuestion_text;
    private String nextAttribute_name;
    private List<String> remain_foodList;
    private String final_recommendation;
}
