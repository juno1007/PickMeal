package com.hankki.pickmeal.domain;

import lombok.Data;

@Data
public class TwentyAttribute {
    private long foodId;
    private int is_spicy;
    private int is_soup;
    private int is_fried;
    private int is_roasted;
    private int has_pork;
    private int has_beef;

}
