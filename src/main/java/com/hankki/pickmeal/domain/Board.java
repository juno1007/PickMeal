package com.hankki.pickmeal.domain;

import lombok.Data;

import java.util.Date;

@Data
public class Board {
    private long boardId;
    private Long user_id;
    private String title;
    private String content;
    private Date createDate;
    private Date updateDate;
    private long likeCount;
    private long dislikeCount;
    private long viewCount;
    private long commentCount;
    private String nickname;
    private String is_notice;
}
