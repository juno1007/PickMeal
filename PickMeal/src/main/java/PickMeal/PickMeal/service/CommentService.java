package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Comment;
import PickMeal.PickMeal.mapper.BoardMapper;
import PickMeal.PickMeal.mapper.CommentMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentMapper commentMapper;
    private final BoardMapper boardMapper;

    public List<Comment> getCommentsByBoardId(long boardId) {
        return commentMapper.getCommentsByBoardId(boardId);
    }

    @Transactional // 중요: 댓글 저장과 숫자 업데이트가 한 번에 성공해야 함
    public void writeComment(Comment comment) {
        // 1. comment 테이블에 댓글 저장
        commentMapper.writeComment(comment);

        // 2. board 테이블의 commentCount 숫자 +1
        boardMapper.plusCommentCount(comment.getBoardId());
    }

    @Transactional // 중요: 삭제와 숫자 감소가 한 세트로 작동해야 함
    public void deleteComment(long comment_id) {
        // 1. 삭제하기 전에 해당 댓글의 정보를 가져옴 (boardId를 알기 위해)
        Comment comment = commentMapper.getCommentByComment_id(comment_id);

        if (comment != null) {
            long boardId = comment.getBoardId();

            // 2. comment 테이블에서 댓글 삭제
            commentMapper.deleteComment(comment_id);

            // 3. board 테이블의 commentCount 숫자 -1 (우리가 만든 메서드)
            boardMapper.minusCommentCount(boardId);
        }
    }

    public Comment getCommentByComment_id(long comment_id) {
        return commentMapper.getCommentByComment_id(comment_id);
    }

    public void updateComment(long comment_id, String content) {
        commentMapper.updateComment(comment_id, content);
    }
}
