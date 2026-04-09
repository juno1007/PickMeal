package com.hankki.pickmeal.service;

import com.hankki.pickmeal.domain.Board;
import com.hankki.pickmeal.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {
    private final BoardMapper boardMapper;

    /**
     * 상단 고정 공지사항 전용 메서드
     */
    public List<Board> findNoticeTop() {
        return boardMapper.findNoticeTop();
    }

    /**
     * 일반 게시글만 가져오기 (is_notice = 'N')
     * 기존 findBoardAll의 역할을 대신합니다.
     */
    public Page<Board> findCommonBoard(Pageable pageable) {
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        // 1. 일반 게시글 리스트 조회
        List<Board> boards = boardMapper.findCommonBoard(offset, pageSize);

        // 2. 일반 게시글의 총 개수 조회 (페이징 계산용)
        int total = boardMapper.countCommonBoard();

        return new PageImpl<>(boards, pageable, total);
    }

    public void writeBoard(Board board) {
        boardMapper.writeBoard(board);
    }

    /**
     * 게시글 삭제 (관리자 권한 체크 포함)
     */
    public void removeBoard(long boardId, String loginUserId, String role) {
        Board board = boardMapper.getBoardByBoardId(boardId);
        if (board == null) throw new IllegalArgumentException("해당 게시글이 없습니다.");

        boolean isAdmin = "ADMIN".equals(role);
        // 이제 loginUserId로 "1"이 들어오므로 board.getUser_id()인 1과 .equals() 비교가 성공합니다!
        boolean isAuthor = board.getUser_id() != null && String.valueOf(board.getUser_id()).equals(loginUserId);

        if (isAdmin || isAuthor) {
            boardMapper.removeBoard(boardId);
        } else {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
    }

    public Board getBoardByBoardId(long boardId) {return boardMapper.getBoardByBoardId(boardId);
    }

    public void editBoard(Board board) {
        boardMapper.editBoard(board);
    }

    public void updateViewCount(long boardId) {
        boardMapper.updateViewCount(boardId);
    }

    // 특정 유저가 작성한 모든 게시글 리스트를 가져옵니다.
    public List<Board> findByUser_id(long userId) {
        return boardMapper.findByUser_id(userId);
    }

    public void removeDislikeCount(long boardId) {
        boardMapper.removeDislikeCount(boardId);
    }

    public void removeLikeCount(long boardId) {
        boardMapper.removeLikeCount(boardId);
    }

    public void addLikeCount(long boardId) {
        boardMapper.addLikeCount(boardId);
    }

    public void addDislikeCount(long boardId) {
        boardMapper.addDislikeCount(boardId);
    }

    public Page<Board> searchCommonBoards(String searchType, String keyword, Pageable pageable) {
        // 1. 매퍼에게 전달할 offset과 pageSize 추출
        int offset = (int) pageable.getOffset();
        int pageSize = pageable.getPageSize();

        List<Board> content;
        long total;

        // 2. 검색어가 있는지 확인 후 각각 다른 매퍼 메서드 호출
        if (keyword == null || keyword.isEmpty()) {
            // 기존에 있던 findCommonBoard 호출
            content = boardMapper.findCommonBoard(offset, pageSize);
            total = boardMapper.countCommonBoard(); // 전체 개수를 가져오는 쿼리 필요
        } else {
            // 새로 만들 검색용 매퍼 메서드 호출
            content = boardMapper.searchCommonBoard(searchType, keyword, offset, pageSize);
            total = boardMapper.countSearchBoard(searchType, keyword); // 검색된 개수 쿼리 필요
        }

        // 3. PageImpl 객체로 감싸서 반환 (컨트롤러의 Page<Board> 타입을 맞추기 위함)
        return new PageImpl<>(content, pageable, total);
    }
}
