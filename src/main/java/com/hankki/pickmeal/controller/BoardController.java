package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.Board;
import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.service.*;
import com.hankki.pickmeal.service.BoardReactionService;
import com.hankki.pickmeal.service.BoardService;
import com.hankki.pickmeal.service.CommentService;
import com.hankki.pickmeal.service.UserService;
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

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    private final UserService userService;
    private final BoardReactionService boardReactionService;
    private final CommentService commentService;

    @GetMapping("/list")
    public String showBoard(
            @PageableDefault(size = 10, sort = "boardId", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {

        List<Board> notices = boardService.findNoticeTop();

        Page<Board> commonBoards = boardService.searchCommonBoards(searchType, keyword, pageable);

        List<String> user_nicknames = commonBoards.stream()
                .map(board -> {
                    User user = userService.findByUser_id(board.getUser_id());
                    return (user != null) ? user.getNickname() : "알 수 없음";
                })
                .toList();

        model.addAttribute("notices", notices);
        model.addAttribute("boards", commonBoards);
        model.addAttribute("user_nicknames", user_nicknames);
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
        User loginUser = userService.getAuthenticatedUser(authentication);

        if (loginUser == null) {
            return "redirect:/users/login";
        }

        board.setUser_id(loginUser.getUser_id());

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            if (board.getIs_notice() == null) {
                board.setIs_notice("N");
            }
        } else {
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
            userReaction = boardReactionService.isLikeOrDislike(boardId, loginUser.getUser_id());

            isWriter = java.util.Objects.equals(board.getUser_id(), loginUser.getUser_id());

            model.addAttribute("user", loginUser);
        }

        model.addAttribute("board", board);
        model.addAttribute("isWriter", isWriter); // 블록 밖에서 명시적으로 전달
        model.addAttribute("userReaction", userReaction);
        model.addAttribute("comments", commentService.getCommentsByBoardId(boardId));


        return "/board/board-detail";
    }

    @PostMapping("/reaction/{boardId}")
    @ResponseBody
    public ResponseEntity<String> boardLikeOrDislikeReaction(@PathVariable Long boardId, Authentication authentication, @RequestParam("like_type") int like_type) {
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
        return "/board/board-edit";
    }

    @PostMapping("/edit/{boardId}")
    public String editBoard(Board board, @PathVariable Long boardId) {
        Board existingBoard = boardService.getBoardByBoardId(boardId);
        board.setBoardId(boardId);
        board.setUser_id(existingBoard.getUser_id());

        if (board.getIs_notice() == null) {
            board.setIs_notice(existingBoard.getIs_notice());
        }

        boardService.editBoard(board);

        return "redirect:/board/detail/" + boardId;
    }

    @PostMapping("/remove/{boardId}")
    public Object removeBoard(@PathVariable Long boardId,
                              @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/users/login";
        }

        User loginUser = userService.getAuthenticatedUser(authentication);
        if (loginUser == null) {
            return "redirect:/users/login";
        }

        String role = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? "ADMIN" : "USER";

        String userId = String.valueOf(loginUser.getUser_id());

        boardService.removeBoard(boardId, userId, role);

        if ("XMLHttpRequest".equals(requestedWith)) {
            return ResponseEntity.ok("Deleted Successfully");
        } else {
            return "redirect:/board/list"; // 게시판에서 삭제 시 목록으로 이동
        }
    }
}