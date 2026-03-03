package PickMeal.PickMeal.controller;

import PickMeal.PickMeal.domain.Board;
import PickMeal.PickMeal.domain.User;
import PickMeal.PickMeal.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final FileService fileService;
    private final UserService userService;
    private final BoardReactionService boardReactionService;
    private final CommentService commentService;

    // [수정] 공통 도우미 메서드: 유저 식별값(String)을 반환합니다.
    private String getLoginUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;

        // 복잡한 타입 체크 대신 시큐리티가 제공하는 기본 Name(ID)을 사용합니다.
        return authentication.getName();
    }

    @GetMapping("/list")
    public String showBoard(
            @PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        // 1. 상단 고정용 공지사항 (검색 시에도 유지할 경우)
        List<Board> notices = boardService.findNoticeTop();

        // 2. 일반 게시글 검색 및 페이징 처리
        // 기존 findCommonBoard 대신 검색 조건이 포함된 메서드를 호출합니다.
        Page<Board> commonBoards = boardService.searchCommonBoards(searchType, keyword, pageable);

        // 3. 닉네임 리스트 추출 (공지사항 + 일반 게시글 통합 처리 추천)
        // 팁: Board 객체 자체에 nickname 필드가 있다면 이 과정은 서비스 단에서 JOIN으로 처리하는 게 성능상 훨씬 좋습니다.
        List<String> user_nicknames = commonBoards.stream()
                .map(board -> {
                    // 이제 userService.findByUser_id는 User 객체를 반환합니다!
                    User user = userService.findByUser_id(board.getUser_id());
                    return (user != null) ? user.getNickname() : "알 수 없음";
                })
                .toList();

        // 4. 모델에 담기
        model.addAttribute("notices", notices);
        model.addAttribute("boards", commonBoards);
        model.addAttribute("user_nicknames", user_nicknames);

        // 검색창에 입력했던 값을 그대로 유지하기 위해 다시 전달
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "/board/board";
    }

    @GetMapping("/write")
    public String writeBoardForm() {
        return "/board/board-write";
    }

    @PostMapping("/write")
    public String writeBoard(Board board, Authentication authentication) {
        // 1. 공통 유저 정보 가져오기 (관리자든 일반 유저든 이제 User 객체로 반환됨)
        User loginUser = userService.getAuthenticatedUser(authentication);

        if (loginUser == null) {
            return "redirect:/users/login";
        }

        // 작성자 ID 세팅 (통합된 user_id 사용)
        board.setUser_id(loginUser.getUser_id());

        // 2. 권한 확인하여 공지사항 여부 결정
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // 관리자가 체크박스를 선택했다면 'Y', 아니면 'N' (HTML input name="is_notice"와 매핑)
            if (board.getIs_notice() == null) {
                board.setIs_notice("N");
            }
        } else {
            // 일반 유저는 체크박스를 조작했더라도 무조건 'N'으로 강제
            board.setIs_notice("N");
        }

        boardService.writeBoard(board);
        return "redirect:/board/list";
    }

    @GetMapping("/detail/{boardId}")
    public String showBoardDetail(@PathVariable Long boardId, Authentication authentication, Model model) {
        boardService.updateViewCount(boardId);
        Board board = boardService.getBoardByBoardId(boardId);
        User loginUser = userService.getAuthenticatedUser(authentication);

        boolean isWriter = false;
        int userReaction = 0;

        if (loginUser != null) {
            // 1. 좋아요 상태 확인
            userReaction = boardReactionService.isLikeOrDislike(boardId, loginUser.getUser_id());

            // 2. 작성자 비교 (Objects.equals 사용으로 널 체크와 타입 비교를 동시에)
            isWriter = java.util.Objects.equals(board.getUser_id(), loginUser.getUser_id());

            // 3. 디버깅용 로그 (서버 콘솔에서 확인 필수!)
            System.out.println("=== 작성자 확인 로그 ===");
            System.out.println("게시글 작성자 PK: " + board.getUser_id());
            System.out.println("현재 로그인 유저 PK: " + loginUser.getUser_id());
            System.out.println("결과 (isWriter): " + isWriter);

            model.addAttribute("user", loginUser);
        }

        model.addAttribute("board", board);
        model.addAttribute("isWriter", isWriter); // 블록 밖에서 명시적으로 전달
        model.addAttribute("userReaction", userReaction);
        model.addAttribute("comments", commentService.getCommentsByBoardId(boardId));
        model.addAttribute("files", fileService.findByBoardId(boardId));

        return "/board/board-detail";
    }

    @PostMapping("/reaction/{boardId}")
    @ResponseBody
    public ResponseEntity<String> boardLikeOrDislikeReaction(@PathVariable Long boardId, Authentication authentication, @RequestParam("like_type") int like_type) {
        // [수정] 중복 제거된 공통 메서드 활용
        User user = userService.getAuthenticatedUser(authentication);

        if (user == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");

        int currentStatus = boardReactionService.isLikeOrDislike(boardId, user.getUser_id());

        if (like_type > 0) {
            if (currentStatus < 0) {
                boardService.removeDislikeCount(boardId);
                boardService.addLikeCount(boardId);
                boardReactionService.removeLikeOrDislikeReaction(boardId, user.getUser_id());
                boardReactionService.boardLikeOrDislikeReaction(boardId, user.getUser_id(), 1);

            } else if (currentStatus > 0) {
                boardService.removeLikeCount(boardId);
                boardReactionService.removeLikeOrDislikeReaction(boardId, user.getUser_id());

            } else {
                boardService.addLikeCount(boardId);
                boardReactionService.boardLikeOrDislikeReaction(boardId, user.getUser_id(), 1);
            }
        } else if (like_type < 0) {
            if (currentStatus > 0) {
                boardService.removeLikeCount(boardId);
                boardReactionService.removeLikeOrDislikeReaction(boardId, user.getUser_id());
                boardService.addDislikeCount(boardId);
                boardReactionService.boardLikeOrDislikeReaction(boardId, user.getUser_id(), -1);

            } else if (currentStatus < 0) {
                boardService.removeDislikeCount(boardId);
                boardReactionService.removeLikeOrDislikeReaction(boardId, user.getUser_id());
            } else {
                boardService.addDislikeCount(boardId);
                boardReactionService.boardLikeOrDislikeReaction(boardId, user.getUser_id(), -1);
            }
        }
        return ResponseEntity.ok("SUCCESS");
    }

    @GetMapping("/edit/{boardId}")
    public String editBoardForm(@PathVariable Long boardId, Model model) {
        Board board = boardService.getBoardByBoardId(boardId);
        model.addAttribute("board", board);
        model.addAttribute("files", fileService.findByBoardId(boardId));
        return "/board/board-edit";
    }

    @PostMapping("/edit/{boardId}")
    public String editBoard(Board board, @PathVariable Long boardId, @RequestParam(value = "file", required = false) MultipartFile file) {
        // 1. 기존 게시글 정보를 먼저 가져옵니다. (작성자 ID 보존을 위해)
        Board existingBoard = boardService.getBoardByBoardId(boardId);

        // 2. 폼에서 넘어오지 않는 필수 정보(작성자 ID)를 다시 세팅합니다.
        board.setBoardId(boardId);
        board.setUser_id(existingBoard.getUser_id()); // 이 줄이 없으면 마이페이지에서 사라집니다!

        // 3. 만약 공지사항 여부(is_notice)가 폼에서 안 넘어온다면 기존 값을 유지합니다.
        if (board.getIs_notice() == null) {
            board.setIs_notice(existingBoard.getIs_notice());
        }

        boardService.editBoard(board);

        if (file != null && !file.isEmpty()) {
            fileService.deleteByBoardId(boardId);
            fileService.saveFile(boardId, file);
        }
        return "redirect:/board/detail/" + boardId;
    }

    @PostMapping("/remove/{boardId}")
    public String removeBoard(@PathVariable Long boardId, Authentication authentication) {
        // 1. 로그인 여부 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        // 2. 현재 로그인한 유저 정보 및 권한 파악
        // SecurityService에서 만든 로직을 활용해 User 객체를 가져옵니다.
        User loginUser = userService.getAuthenticatedUser(authentication);

        if (loginUser == null) {
            return "redirect:/users/login";
        }

        // 3. 권한 및 ID 추출
        String role = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "USER";

        // 일반 유저라면 본인 확인을 위해 ID를 넘기고, 관리자라면 검증을 건너뛰도록 처리
        String userId = loginUser.getId();

        // 4. 서비스 호출 (기존 role 기반 로직 유지)
        // 관리자일 경우 서비스 단에서 이 role을 보고 본인 확인 로직을 생략하게 구현되어 있어야 합니다.
        boardService.removeBoard(boardId, userId, role);
        fileService.deleteByBoardId(boardId);

        return "redirect:/board/list";
    }
}