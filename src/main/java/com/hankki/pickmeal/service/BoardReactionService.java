package com.hankki.pickmeal.service;

import com.hankki.pickmeal.mapper.BoardReactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardReactionService {
    private final BoardReactionMapper boardReactionMapper;

    public void boardLikeOrDislikeReaction(long boardId, long user_id, int like_type) {
        boardReactionMapper.boardLikeOrDislikeReaction(boardId, user_id, like_type);
    }

    public int isLikeOrDislike(long boardId, long user_id) {
        if(boardReactionMapper.isLikeOrDislike(boardId, user_id) == null){
            return 0;
        }

        return boardReactionMapper.isLikeOrDislike(boardId, user_id);
    }

    public void removeLikeOrDislikeReaction(long boardId, long user_id) {
        boardReactionMapper.removeLikeOrDislikeReaction(boardId, user_id);
    }
}
