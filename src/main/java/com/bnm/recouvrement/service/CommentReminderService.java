package com.bnm.recouvrement.service;

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
        ScheduledExecutorService commentScheduler = Executors.newSingleThreadScheduledExecutor();
        reminderTasks.put(comment.getId(), commentScheduler);

        long delayInMillis;
        if (comment.getReminderDateTime() != null) {
            // Calculer le délai jusqu'à la date/heure choisie
            long now = System.currentTimeMillis();
            long target = comment.getReminderDateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            delayInMillis = target - now;
            if (delayInMillis < 0) delayInMillis = 0; // Si la date est passée, envoyer tout de suite
        } else {
            // Par défaut : 1 minute (test) ou 3 jours (prod)
            delayInMillis = 60 * 1000; // 1 minute
            // delayInMillis = 3L * 24 * 60 * 60 * 1000; // 3 jours
        }
        commentScheduler.schedule(() -> {
            try {
                sendReminderEmail(comment, user);
                commentScheduler.shutdown();
                reminderTasks.remove(comment.getId());
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du rappel pour le commentaire #" + comment.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }, delayInMillis, TimeUnit.MILLISECONDS);
        System.out.println("Rappel planifié pour le commentaire #" + comment.getId() + " dans " + delayInMillis + " ms");
    }
    
    /**
     * Envoie l'e-mail de rappel pour un commentaire.
     */
    private void sendReminderEmail(Comment comment, User user) throws MessagingException {
        String to = user.getEmail();
        String subject = "Rappel : Votre commentaire sur le dossier #" + comment.getDossier().getId();
        
        // Vérifier si le commentaire contient une mention de changement de statut
        String statusInfo = "";
        String commentContent = comment.getContent();
        
        if (commentContent.contains("[Changement de statut :")) {
            int startIndex = commentContent.indexOf("[Changement de statut :");
            int endIndex = commentContent.indexOf("]", startIndex);
            if (endIndex > startIndex) {
                String statusChange = commentContent.substring(startIndex, endIndex + 1);
                statusInfo = "<p><strong>" + statusChange + "</strong></p>";
                // Enlever la partie du statut du contenu du commentaire pour l'affichage
                commentContent = commentContent.substring(endIndex + 1).trim();
            }
        }
        
        String content = "<html><body>"
                + "<h2>Rappel de commentaire</h2>"
                + "<p>Bonjour " + user.getName() + ",</p>"
                + "<p>Vous avez ajouté un commentaire sur le dossier #" + comment.getDossier().getId() + " le " 
                + comment.getCreatedAt().toString() + ".</p>"
                + statusInfo
                + "<p><strong>Votre commentaire :</strong></p>"
                + "<blockquote>" + commentContent + "</blockquote>"
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
