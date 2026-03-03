package PickMeal.PickMeal.mapper;

import PickMeal.PickMeal.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment> getCommentsByBoardId(long boardId);

    void writeComment(Comment comment);

    void deleteComment(long comment_id);

    Comment getCommentByComment_id(long comment_id);

    void updateComment(long comment_id, String content);
}
