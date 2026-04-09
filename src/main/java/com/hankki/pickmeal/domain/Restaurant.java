package com.hankki.pickmeal.domain;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter // 데이터를 넣고 빼는 기능을 자동으로 만들어주는 도구예요.
public class Restaurant {
    private Long res_id;                // 고유 번호표 (ID)
    private String res_name;            // 식당 이름
    private String address;             // 식당 주소
    private Double lon;                 // 경도 (X좌표)
    private Double lat;                 // 위도 (Y좌표)
    private String region_name;         // 지역명 (경기, 강원 등)
    private String representative_menu; // 대표 메뉴 이름
    private Integer menu_price;         // 메뉴 가격
}

