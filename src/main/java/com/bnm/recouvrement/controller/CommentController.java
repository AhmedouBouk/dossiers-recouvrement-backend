package com.bnm.recouvrement.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

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
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.CommentService;
import com.bnm.recouvrement.service.DossierRecouvrementService;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/dossiers")
public class CommentController {

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private DossierRecouvrementService dossierService;

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
        // Récupérer la date/heure du rappel (optionnelle)
        String reminderDateTimeStr = payload.get("reminderDateTime");
        LocalDateTime reminderDateTime = null;
        if (reminderDateTimeStr != null && !reminderDateTimeStr.isEmpty()) {
            try {
                reminderDateTime = LocalDateTime.parse(reminderDateTimeStr);
            } catch (Exception e) {
                // En cas d'erreur de parsing, on laisse reminderDateTime à null (pas de rappel)
                System.err.println("Erreur lors du parsing de la date de rappel: " + e.getMessage());
            }
        }
        Comment comment = commentService.addComment(dossierId, content, reminderDateTime);
        return ResponseEntity.ok(new CommentDTO(comment));
    }
    
    /**
     * Met à jour le statut d'un dossier et ajoute un commentaire en même temps
     * @param dossierId ID du dossier à mettre à jour
     * @param payload Contient le nouveau statut et le commentaire
     * @return Le commentaire ajouté
     */
    @PostMapping("/{dossierId}/status-with-comment")
    public ResponseEntity<CommentDTO> updateStatusWithComment(
            @PathVariable Long dossierId,
            @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        String content = payload.get("content");
        // Récupérer la date/heure du rappel (optionnelle)
        String reminderDateTimeStr = payload.get("reminderDateTime");
        LocalDateTime reminderDateTime = null;
        if (reminderDateTimeStr != null && !reminderDateTimeStr.isEmpty()) {
            try {
                reminderDateTime = LocalDateTime.parse(reminderDateTimeStr);
            } catch (Exception e) {
                // En cas d'erreur de parsing, on laisse reminderDateTime à null (pas de rappel)
                System.err.println("Erreur lors du parsing de la date de rappel: " + e.getMessage());
            }
        }
        // Mettre à jour le statut du dossier
        dossierService.updateDossierStatus(dossierId, newStatus);
        // Ajouter un commentaire avec mention du changement de statut
        String commentWithStatus = "[Changement de statut : " + newStatus + "] " + content;
        Comment comment = commentService.addComment(dossierId, commentWithStatus, reminderDateTime);
        return ResponseEntity.ok(new CommentDTO(comment));
    }
}