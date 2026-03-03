package PickMeal.PickMeal.service;

import PickMeal.PickMeal.domain.Board;
import PickMeal.PickMeal.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public void removeBoard(long boardId, String loginUserId, String role) { // 파라미터 타입을 String으로 변경
        // 1. 게시글 존재 여부 확인
        Board board = boardMapper.getBoardByBoardId(boardId);

        if (board == null) {
            log.warn("삭제 실패: {}번 게시글이 존재하지 않습니다.", boardId);
            throw new IllegalArgumentException("해당 게시글이 없습니다.");
        }

        // 2. 권한 체크 로직
        // 관리자("ADMIN")이거나, 게시글의 작성자(user_id)와 현재 로그인한 아이디가 일치하는지 확인
        boolean isAdmin = "ADMIN".equals(role);

        // [중요] 문자열 비교이므로 .equals()를 사용합니다.
        boolean isAuthor = board.getUser_id() != null && board.getUser_id().equals(loginUserId);

        if (isAdmin || isAuthor) {
            // 3. 삭제 실행
            boardMapper.removeBoard(boardId);
            log.info("[삭제 성공] 접속ID: {}, 권한: {}, 글번호: {}", loginUserId, role, boardId);
        } else {
            // 본인도 아니고 관리자도 아닌 경우
            log.error("[권한 오류] 유저 {}가 {}번 게시글 삭제 시도 (작성자: {})", loginUserId, boardId, board.getUser_id());
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
    }

    public List<Long> getBoardIdByUser_id(long userId) {return boardMapper.getBoardIdByUser_id(userId);
    }

    public Board getBoardByBoardId(long boardId) {return boardMapper.getBoardByBoardId(boardId);
    }

    public void editBoard(Board board) {
        boardMapper.editBoard(board);
    }

    public List<String> extractImageUrlFromContent(String content) {
        if(content == null || content.isEmpty()) return new ArrayList<>();

        Document doc = Jsoup.parse(content);
        Elements elements = doc.getElementsByTag("img");

        List<String> imageUrls = new ArrayList<>();
        for (Element element : elements){
            imageUrls.add(element.attr("src"));
        }

        return imageUrls;
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
