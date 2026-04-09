package com.hankki.pickmeal.controller;

import com.hankki.pickmeal.domain.Comment;
import com.hankki.pickmeal.domain.User;
import com.hankki.pickmeal.service.CommentService;
import com.hankki.pickmeal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    @PostMapping("/write/{boardId}")
    public String writeComment(@PathVariable Long boardId, Comment comment, Authentication authentication) {
        User user = userService.getAuthenticatedUser(authentication);
        if (user != null) {
            comment.setBoardId(boardId);
            comment.setUser_id(user.getUser_id());
            commentService.writeComment(comment);
        }
        return "redirect:/board/detail/" + boardId;
    }

    @PostMapping("/delete/{comment_id}")
    public String deleteComment(@PathVariable long comment_id, Authentication authentication) {
        Comment comment = commentService.getCommentByComment_id(comment_id);
        User user = userService.getAuthenticatedUser(authentication);

        if (comment == null) return "redirect:/board/list";

        if (user != null && java.util.Objects.equals(comment.getUser_id(), user.getUser_id())) {
            commentService.deleteComment(comment_id);
        }
        return "redirect:/board/detail/" + comment.getBoardId();
    }

    @PostMapping("/update/{comment_id}")
    public String updateComment(@PathVariable long comment_id,
                                @RequestParam("content") String content,
                                Authentication authentication) {
        Comment comment = commentService.getCommentByComment_id(comment_id);
        User user = userService.getAuthenticatedUser(authentication);

        if (comment == null || user == null) return "redirect:/board/list";

        if (java.util.Objects.equals(comment.getUser_id(), user.getUser_id())) {
            commentService.updateComment(comment_id, content);
        }

        return "redirect:/board/detail/" + comment.getBoardId();
    }
}
