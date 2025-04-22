package com.bnm.recouvrement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Comment;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.repository.CommentRepository;
import com.bnm.recouvrement.service.CommentReminderService;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private CommentReminderService reminderService;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByDossierId(Long dossierId) {
        return commentRepository.findByDossierIdOrderByCreatedAtDesc(dossierId);
    }

    @Transactional
    public Comment addComment(Long dossierId, String content) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found"));
        
        // Récupérer l'utilisateur actuellement authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setDossier(dossier);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(currentUser); // Associer l'utilisateur au commentaire
        
        // Sauvegarder le commentaire
        Comment savedComment = commentRepository.save(comment);
        
        // Planifier un rappel par e-mail
        reminderService.scheduleCommentReminder(savedComment, currentUser);
        
        return savedComment;
    }
}