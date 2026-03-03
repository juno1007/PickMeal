package PickMeal.PickMeal.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Game {
    private Long gameId;
    private Long user_id;
    private Long food_id;
    private String gameType;
    private LocalDateTime playDate;

}
