package com.hankki.pickmeal.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BoardReactionMapper {

    void boardLikeOrDislikeReaction(long boardId, long user_id, int like_type);

    Integer isLikeOrDislike(long boardId, long user_id);

    void removeLikeOrDislikeReaction(long boardId, long user_id);
}
