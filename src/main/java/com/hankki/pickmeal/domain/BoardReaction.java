package com.hankki.pickmeal.domain;

import lombok.Data;

@Data
public class BoardReaction {
    private long user_id;
    private long boardId;
    private int like_type;

}
