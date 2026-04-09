package com.hankki.pickmeal.mapper;

import com.hankki.pickmeal.domain.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    List<Board> findNoticeTop();

    // 2. [수정] 일반 게시글 전용 (페이징을 위해 offset과 pageSize를 직접 전달)
    List<Board> findCommonBoard(@Param("offset") int offset, @Param("pageSize") int pageSize);

    // 3. [수정] 일반 게시글의 전체 개수 (페이징 계산용)
    int countCommonBoard();

    void writeBoard(Board board);

    void removeBoard(long boardId);

    Board getBoardByBoardId(long boardId);

    void editBoard(Board board);

    void updateViewCount(long boardId);

    List<Board> findByUser_id(long userId);

    void removeDislikeCount(long boardId);

    void removeLikeCount(long boardId);

    void addLikeCount(long boardId);

    void addDislikeCount(long boardId);

    void plusCommentCount(long boardId);

    void minusCommentCount(long boardId);

    List<Board> searchCommonBoard(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    long countSearchBoard(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword
    );
}
