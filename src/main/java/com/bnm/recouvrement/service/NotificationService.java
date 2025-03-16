package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    public void notifyGarantieUploadRequired(DossierRecouvrement dossier) {
        System.out.println("Tentative d'envoi de notification pour le dossier #" + dossier.getId());
        
        // Récupérer tous les utilisateurs ayant la permission UPLOAD_GARANTIE
        List<User> usersWithPermission = getUsersWithPermission("IMPORT_GRANTIE");
        
        System.out.println("Utilisateurs avec permission UPLOAD_GRANTIE: " + usersWithPermission.size());
        for (User user : usersWithPermission) {
            System.out.println("- Utilisateur éligible: " + user.getEmail() + " (ID: " + user.getId() + ")");
        }
        
        if (usersWithPermission.isEmpty()) {
            System.out.println("Aucun utilisateur à notifier - Vérifiez que des utilisateurs ont la permission UPLOAD_GRANTIE");
            return; // Aucun utilisateur à notifier
        }
        
        String subject = "Action requise: Téléchargement de garantie pour le dossier #" + dossier.getId();
        String content = buildGarantieUploadEmail(dossier);
        
        // Envoyer l'email à chaque utilisateur concerné
        for (User user : usersWithPermission) {
            try {
                emailService.sendEmail(user.getEmail(), subject, content, true);
                System.out.println("Email de notification envoyé à " + user.getEmail() + " pour le dossier #" + dossier.getId());
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi d'email à " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private List<User> getUsersWithPermission(String permissionName) {
        // Récupérer tous les utilisateurs
        List<User> allUsers = userRepository.findAll();
        
        // Filtrer ceux qui ont la permission requise
        return allUsers.stream()
                .filter(user -> user.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals(permissionName)))
                .collect(Collectors.toList());
    }
    
    private String buildGarantieUploadEmail(DossierRecouvrement dossier) {
        // Construire le contenu HTML de l'email
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<h2>Action requise: Téléchargement de garantie</h2>");
        emailContent.append("<p>Un nouveau dossier de recouvrement nécessite le téléchargement d'un fichier de garantie.</p>");
        emailContent.append("<h3>Détails du dossier:</h3>");
        emailContent.append("<ul>");
        emailContent.append("<li><b>Numéro de dossier:</b> ").append(dossier.getId()).append("</li>");
        
        if (dossier.getCompte() != null) {
            emailContent.append("<li><b>Compte:</b> ").append(dossier.getCompte().getNomCompte()).append("</li>");
            
            if (dossier.getCompte().getClient() != null) {
                emailContent.append("<li><b>Client:</b> ").append(dossier.getCompte().getClient().getNom())
                        .append(" ").append(dossier.getCompte().getClient().getPrenom()).append("</li>");
            }
        }
        
        emailContent.append("<li><b>Montant principal:</b> ").append(dossier.getMontantPrincipal()).append("</li>");
        emailContent.append("<li><b>Date de création:</b> ").append(dossier.getDateCreation()).append("</li>");
        emailContent.append("</ul>");
        
        emailContent.append("<p>Veuillez vous connecter à l'application pour télécharger le fichier de garantie requis.</p>");
        emailContent.append("<a href='http://localhost:4200/dossiers/").append(dossier.getId())
                .append("' style='background-color: #4CAF50; color: white; padding: 10px 15px; text-align: center; ")
                .append("text-decoration: none; display: inline-block; border-radius: 5px;'>")
                .append("Accéder au dossier</a>");
        
        emailContent.append("<p>Cordialement,<br>Le système de gestion des recouvrements BNM</p>");
        emailContent.append("</body></html>");
        
        return emailContent.toString();
    }
}