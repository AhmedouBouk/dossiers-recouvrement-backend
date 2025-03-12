package com.bnm.recouvrement.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.dto.CommentDTO;
import com.bnm.recouvrement.entity.Comment;
import com.bnm.recouvrement.service.CommentService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/dossiers")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{dossierId}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long dossierId) {
        List<Comment> comments = commentService.getCommentsByDossierId(dossierId);
        List<CommentDTO> commentDTOs = comments.stream()
            .map(CommentDTO::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(commentDTOs);
    }

    @PostMapping("/{dossierId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long dossierId,
            @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        Comment comment = commentService.addComment(dossierId, content);
        return ResponseEntity.ok(new CommentDTO(comment));
    }
}