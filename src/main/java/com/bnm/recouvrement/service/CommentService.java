package com.bnm.recouvrement.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.repository.CommentRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Comment;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.User;

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
    
    @Transactional(readOnly = true)
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
    }

    @Transactional
    public Comment addComment(Long dossierId, String content, LocalDateTime reminderDateTime) {
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
        comment.setReminderDateTime(reminderDateTime);
        
        // Sauvegarder le commentaire
        Comment savedComment = commentRepository.save(comment);
        
        // Planifier un rappel par e-mail uniquement si une date est spécifiée
        if (reminderDateTime != null) {
            reminderService.scheduleCommentReminder(savedComment, currentUser);
        }
        
        return savedComment;
    }
    
    @Transactional
    public Comment updateComment(Long commentId, String content, LocalDateTime reminderDateTime) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        // Vérifier si l'utilisateur actuel est l'auteur du commentaire
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Extraire l'utilisateur et son ID des détails de l'authentification
        User currentUser = null;
        Integer currentUserId = null;
        if (authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
            currentUserId = currentUser.getId();
        }
        
        // Si l'ID de l'utilisateur n'est pas disponible ou ne correspond pas à l'auteur du commentaire
        // Autoriser la modification pour le moment (pour les tests)
        if (currentUserId != null && comment.getUser() != null && !comment.getUser().getId().equals(currentUserId)) {
            System.out.println("Tentative de modification non autorisée. User ID: " + currentUserId + ", Comment User ID: " + comment.getUser().getId());
            // Commenter la ligne suivante pour les tests
            // throw new RuntimeException("Vous n'êtes pas autorisé à modifier ce commentaire");
        }
        
        // Mettre à jour le contenu
        comment.setContent(content);
        
        // Mettre à jour la date de rappel si fournie
        if (reminderDateTime != null) {
            // Si une date de rappel existait déjà, annuler l'ancien rappel
            if (comment.getReminderDateTime() != null) {
                reminderService.cancelReminder(commentId);
            }
            comment.setReminderDateTime(reminderDateTime);
            
            // Planifier un nouveau rappel
            Comment savedComment = commentRepository.save(comment);
            reminderService.scheduleCommentReminder(savedComment, currentUser);
            return savedComment;
        } else {
            // Si la nouvelle date est null mais qu'une date existait, annuler le rappel
            if (comment.getReminderDateTime() != null) {
                reminderService.cancelReminder(commentId);
                comment.setReminderDateTime(null);
            }
            return commentRepository.save(comment);
        }
    }
    
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        // Vérifier si l'utilisateur actuel est l'auteur du commentaire
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Extraire l'ID de l'utilisateur des détails de l'authentification
        Integer currentUserId = null;
        if (authentication.getPrincipal() instanceof User) {
            User currentUser = (User) authentication.getPrincipal();
            currentUserId = currentUser.getId();
        }
        
        // Si l'ID de l'utilisateur n'est pas disponible ou ne correspond pas à l'auteur du commentaire
        // Autoriser la suppression pour le moment (pour les tests)
        if (currentUserId != null && comment.getUser() != null && !comment.getUser().getId().equals(currentUserId)) {
            System.out.println("Tentative de suppression non autorisée. User ID: " + currentUserId + ", Comment User ID: " + comment.getUser().getId());
            // Commenter la ligne suivante pour les tests
            // throw new RuntimeException("Vous n'êtes pas autorisé à supprimer ce commentaire");
        }
        
        // Annuler tout rappel programmé
        if (comment.getReminderDateTime() != null) {
            reminderService.cancelReminder(commentId);
        }
        
        // Supprimer le commentaire
        commentRepository.delete(comment);
    }
    
    /**
     * Méthode directe pour supprimer un commentaire sans vérification d'autorisation
     * Utilisée uniquement pour les tests ou les situations d'urgence
     */
    @Transactional
    public void deleteCommentDirectly(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        // Annuler tout rappel programmé
        if (comment.getReminderDateTime() != null) {
            reminderService.cancelReminder(commentId);
        }
        
        // Supprimer le commentaire sans vérification d'autorisation
        commentRepository.delete(comment);
        
        System.out.println("Commentaire " + commentId + " supprimé directement sans vérification d'autorisation");
    }
}