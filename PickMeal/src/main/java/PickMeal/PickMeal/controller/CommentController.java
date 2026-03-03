package PickMeal.PickMeal.controller;

import PickMeal.PickMeal.domain.Comment;
import PickMeal.PickMeal.domain.User;
import PickMeal.PickMeal.service.CommentService;
import PickMeal.PickMeal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;

    // 1. 댓글 작성
    @PostMapping("/write/{boardId}")
    public String writeComment(@PathVariable Long boardId, Comment comment, Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        if (user != null) {
            comment.setBoardId(boardId);
            comment.setUser_id(user.getUser_id()); // [중요] Long 타입 매핑 확인됨
            commentService.writeComment(comment);
        }
        return "redirect:/board/detail/" + boardId;
    }

    // 2. 댓글 삭제
    @PostMapping("/delete/{comment_id}")
    public String deleteComment(@PathVariable long comment_id, Authentication authentication) {
        Comment comment = commentService.getCommentByComment_id(comment_id);
        User user = userService.getAuthenticatedUser(authentication);

        // [방어 코드 1] 댓글이 없는 경우 처리 (NPE 방지)
        if (comment == null) return "redirect:/board/list";

        // [방어 코드 2] 작성자 본인 확인 (Long 객체 비교는 Objects.equals 권장)
        if (user != null && java.util.Objects.equals(comment.getUser_id(), user.getUser_id())) {
            commentService.deleteComment(comment_id);
        }
        return "redirect:/board/detail/" + comment.getBoardId();
    }

    // 3. 댓글 수정
    @PostMapping("/update/{comment_id}")
    public String updateComment(@PathVariable long comment_id,
                                @RequestParam("content") String content, // @RequestParam 명시 권장
                                Authentication authentication) {
        Comment comment = commentService.getCommentByComment_id(comment_id);
        User user = userService.getAuthenticatedUser(authentication);

        if (comment == null || user == null) return "redirect:/board/list";

        // 디버깅 로그 추가: 이 값이 콘솔에 어떻게 찍히는지 보세요!
        System.out.println("댓글 작성자 ID: " + comment.getUser_id());
        System.out.println("현재 유저 ID: " + user.getUser_id());

        // 권한 확인
        if (java.util.Objects.equals(comment.getUser_id(), user.getUser_id())) {
            commentService.updateComment(comment_id, content);
        }

        return "redirect:/board/detail/" + comment.getBoardId();
    }
}
