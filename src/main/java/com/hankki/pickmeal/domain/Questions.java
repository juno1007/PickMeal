package com.hankki.pickmeal.domain;

import lombok.Data;

@Data
public class Questions {
    private Integer question_id;
    private String question_text;
    private String attribute_name;

}
