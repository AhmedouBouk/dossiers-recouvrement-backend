package com.bnm.recouvrement.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bnm.recouvrement.entity.Comment;
import com.bnm.recouvrement.entity.User;
import jakarta.mail.MessagingException;

@Service
public class CommentReminderService {

    @Autowired
    private EmailService emailService;
    
    private final Map<Long, ScheduledExecutorService> reminderTasks = new HashMap<>();

    /**
     * Planifie un rappel par e-mail pour un commentaire.
     * Pour les tests, le rappel est envoyé après 1 minute.
     * En production, il serait envoyé après 3 jours.
     */
    public void scheduleCommentReminder(Comment comment, User user) {
        // Créer un service d'exécution dédié pour ce rappel
        ScheduledExecutorService commentScheduler = Executors.newSingleThreadScheduledExecutor();
        reminderTasks.put(comment.getId(), commentScheduler);
        
        // Pour les tests : 1 minute
        long delayInMinutes = 1;
        // Pour la production : 3 jours (décommentez cette ligne quand vous êtes prêt)
        // long delayInMinutes = 3 * 24 * 60; // 3 jours en minutes
        
        commentScheduler.schedule(() -> {
            try {
                sendReminderEmail(comment, user);
                // Fermer le scheduler après l'envoi
                commentScheduler.shutdown();
                reminderTasks.remove(comment.getId());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du rappel pour le commentaire #" + comment.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, delayInMinutes, TimeUnit.MINUTES);
        
        System.out.println("Rappel planifié pour le commentaire #" + comment.getId() + " dans " + delayInMinutes + " minutes");
    }
    
    /**
     * Envoie l'e-mail de rappel pour un commentaire.
     */
    private void sendReminderEmail(Comment comment, User user) throws MessagingException {
        String to = user.getEmail();
        String subject = "Rappel : Votre commentaire sur le dossier #" + comment.getDossier().getId();
        
        String content = "<html><body>"
                + "<h2>Rappel de commentaire</h2>"
                + "<p>Bonjour " + user.getName() + ",</p>"
                + "<p>Vous avez ajouté un commentaire sur le dossier #" + comment.getDossier().getId() + " le " 
                + comment.getCreatedAt().toString() + ".</p>"
                + "<p><strong>Votre commentaire :</strong></p>"
                + "<blockquote>" + comment.getContent() + "</blockquote>"
                + "<p>Ce rappel est envoyé automatiquement pour vous aider à suivre vos actions sur les dossiers.</p>"
                + "<p>Vous pouvez consulter le dossier en <a href='http://localhost:4200/dossiers/" + comment.getDossier().getId() + "'>cliquant ici</a>.</p>"
                + "<p>Cordialement,<br>L'équipe BNM</p>"
                + "</body></html>";
        
        emailService.sendEmail(to, subject, content, true);
        System.out.println("Email de rappel envoyé à " + to + " pour le commentaire #" + comment.getId());
    }
    
    /**
     * Annule un rappel planifié pour un commentaire.
     */
    public void cancelReminder(Long commentId) {
        ScheduledExecutorService commentScheduler = reminderTasks.get(commentId);
        if (commentScheduler != null) {
            commentScheduler.shutdownNow();
            reminderTasks.remove(commentId);
            System.out.println("Rappel annulé pour le commentaire #" + commentId);
        }
    }
}
