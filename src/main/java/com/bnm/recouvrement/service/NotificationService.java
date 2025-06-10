package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.repository.NotificationRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.Agence;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.Notification;
import com.bnm.recouvrement.entity.User;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AgenceRepository agenceRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;
    
    public void notifyGarantieUploadRequired(DossierRecouvrement dossier) {
        System.out.println("Tentative d'envoi de notification pour le dossier #" + dossier.getId());
        
        // Récupérer tous les utilisateurs ayant la permission IMPORT_GRANTIE
        List<User> usersWithPermission = getUsersWithPermission("IMPORT_GRANTIE");
        
        System.out.println("Utilisateurs avec permission IMPORT_GRANTIE: " + usersWithPermission.size());
        for (User user : usersWithPermission) {
            System.out.println("- Utilisateur éligible: " + user.getEmail() + " (ID: " + user.getId() + ")");
        }
        
        if (usersWithPermission.isEmpty()) {
            System.out.println("Aucun utilisateur à notifier - Vérifiez que des utilisateurs ont la permission IMPORT_GRANTIE");
            return; // Aucun utilisateur à notifier
        }
        
        String subject = "Action requise: Téléchargement de garantie pour le dossier #" + dossier.getId();
        String content = buildGarantieUploadEmail(dossier);
        
        // Envoyer l'email à chaque utilisateur concerné et créer une notification
        for (User user : usersWithPermission) {
            try {
                emailService.sendEmail(user.getEmail(), subject, content, true);
                System.out.println("Email de notification envoyé à " + user.getEmail() + " pour le dossier #" + dossier.getId());
                
                // Créer et sauvegarder une notification
                Notification notification = new Notification(
                    user,
                    dossier,
                    "Téléchargement de garantie requis",
                    "Une garantie doit être téléchargée pour le dossier #" + dossier.getId(),
                    "GARANTIE"
                );
                notificationRepository.save(notification);
                
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi d'email à " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Méthode pour notification chèque par agence
    public void notifyChequeUploadRequired(DossierRecouvrement dossier) {
        System.out.println("Tentative d'envoi de notification chèque pour le dossier #" + dossier.getId());
        
        // Vérifier si la référence du chèque existe
        if (dossier.getReferencesChecks() == null || dossier.getReferencesChecks().isEmpty()) {
            System.out.println("Aucune référence de chèque pour le dossier #" + dossier.getId());
            return;
        }
        
        // Extraire le code de l'agence de la référence du chèque (format: TT2405300443/12/kl)
        String[] parts = dossier.getReferencesChecks().split("/");
        if (parts.length < 2) {
            System.out.println("Format de référence de chèque invalide: " + dossier.getReferencesChecks());
            return;
        }
        
        String agenceCode = parts[1];
        System.out.println("Code agence extrait: " + agenceCode);
        
        // Rechercher l'agence par code
        Optional<Agence> agenceOpt = agenceRepository.findByCode(agenceCode);
        if (agenceOpt.isEmpty()) {
            System.out.println("Aucune agence trouvée avec le code: " + agenceCode);
            return;
        }
        
        Agence agence = agenceOpt.get();
        
        // Récupérer tous les utilisateurs de cette agence
        List<User> usersInAgence = userRepository.findAll().stream()
                .filter(user -> user.getAgence() != null && user.getAgence().getId().equals(agence.getId()))
                .collect(Collectors.toList());
        
        System.out.println("Utilisateurs appartenant à l'agence " + agence.getNom() + " (code: " + agence.getCode() + "): " + usersInAgence.size());
        
        if (usersInAgence.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé pour l'agence: " + agence.getNom());
            return;
        }
        
        // Créer le sujet et le contenu de l'email
        String subject = "Action requise: Téléchargement de chèque pour le dossier #" + dossier.getId();
        String content = buildChequeUploadEmail(dossier, agence.getNom());
        
        // Envoyer l'email à tous les utilisateurs de l'agence
        for (User user : usersInAgence) {
            try {
                emailService.sendEmail(user.getEmail(), subject, content, true);
                System.out.println("Email de notification chèque envoyé à " + user.getEmail() + " pour le dossier #" + dossier.getId());
                
                // Créer et sauvegarder une notification
                Notification notification = new Notification(
                    user,
                    dossier,
                    "Téléchargement de chèque requis",
                    "Un chèque (Réf: " + dossier.getReferencesChecks() + ") doit être téléchargé pour le dossier #" + dossier.getId(),
                    "CHEQUE"
                );
                notificationRepository.save(notification);
                
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi d'email à " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Nouvelle méthode pour notification de documents DO (caution, LC, crédit)
    public void notifyDoDocumentsUploadRequired(DossierRecouvrement dossier) {
        System.out.println("Tentative d'envoi de notification documents DO pour le dossier #" + dossier.getId());
        
        // Vérifier si au moins une référence existe
        boolean hasReferenceCaution = dossier.getReferencesCautions() != null && !dossier.getReferencesCautions().isEmpty();
        boolean hasReferenceLC = dossier.getReferencesLC() != null && !dossier.getReferencesLC().isEmpty();
        boolean hasReferenceCredit = dossier.getReferencesCredits() != null && !dossier.getReferencesCredits().isEmpty();
        
        if (!hasReferenceCaution && !hasReferenceLC && !hasReferenceCredit) {
            System.out.println("Aucune référence de document DO pour le dossier #" + dossier.getId());
            return;
        }
        
        // Récupérer tous les utilisateurs de type DO
        List<User> doUsers = userRepository.findAll().stream()
                .filter(user -> "Do".equals(user.getUserType()))
                .collect(Collectors.toList());
        
        System.out.println("Utilisateurs de type DO: " + doUsers.size());
        
        if (doUsers.isEmpty()) {
            System.out.println("Aucun utilisateur de type DO trouvé");
            return;
        }
        
        // Créer la liste des documents requis
        List<String> requiredDocuments = new ArrayList<>();
        if (hasReferenceCaution && dossier.getCautionsFile() == null) requiredDocuments.add("Caution");
        if (hasReferenceLC && dossier.getLcFile() == null) requiredDocuments.add("Lettre de Crédit (LC)");
        if (hasReferenceCredit && dossier.getCreditsFile() == null) requiredDocuments.add("Crédit");
        
        if (requiredDocuments.isEmpty()) {
            System.out.println("Tous les documents DO sont déjà téléchargés pour le dossier #" + dossier.getId());
            return;
        }
        
        String subject = "Action requise: Téléchargement de documents pour le dossier #" + dossier.getId();
        String content = buildDoDocumentsUploadEmail(dossier, requiredDocuments);
        
        // Description courte pour la notification
        String docsListStr = String.join(", ", requiredDocuments);
        
        // Envoyer l'email à tous les utilisateurs DO
        for (User user : doUsers) {
            try {
                emailService.sendEmail(user.getEmail(), subject, content, true);
                System.out.println("Email de notification documents DO envoyé à " + user.getEmail() + " pour le dossier #" + dossier.getId());
                
                // Créer et sauvegarder une notification
                Notification notification = new Notification(
                    user,
                    dossier,
                    "Documents requis: " + docsListStr,
                    "Les documents suivants doivent être téléchargés pour le dossier #" + dossier.getId() + ": " + docsListStr,
                    "DOCUMENT_DO"
                );
                notificationRepository.save(notification);
                
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
    
    private String buildChequeUploadEmail(DossierRecouvrement dossier, String agenceName) {
        // Nouveau contenu pour chèque
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<h2>Action requise: Téléchargement de chèque</h2>");
        emailContent.append("<p>Un dossier de recouvrement nécessite le téléchargement d'un fichier de chèque par l'agence " + agenceName + ".</p>");
        emailContent.append("<h3>Détails du dossier:</h3>");
        emailContent.append("<ul>");
        emailContent.append("<li><b>Numéro de dossier:</b> ").append(dossier.getId()).append("</li>");
        emailContent.append("<li><b>Référence chèque:</b> ").append(dossier.getReferencesChecks()).append("</li>");
        
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
        
        emailContent.append("<p>En tant que membre de l'agence " + agenceName + ", veuillez vous connecter à l'application pour télécharger le fichier de chèque requis.</p>");
        emailContent.append("<a href='http://localhost:4200/dossiers/").append(dossier.getId())
                .append("' style='background-color: #4CAF50; color: white; padding: 10px 15px; text-align: center; ")
                .append("text-decoration: none; display: inline-block; border-radius: 5px;'>")
                .append("Accéder au dossier</a>");
        
        emailContent.append("<p>Cordialement,<br>Le système de gestion des recouvrements BNM</p>");
        emailContent.append("</body></html>");
        
        return emailContent.toString();
    }
    
    private String buildDoDocumentsUploadEmail(DossierRecouvrement dossier, List<String> requiredDocuments) {
        // Contenu pour documents DO
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<h2>Action requise: Téléchargement de documents</h2>");
        emailContent.append("<p>Un dossier de recouvrement nécessite le téléchargement des documents suivants:</p>");
        
        emailContent.append("<ul>");
        for (String doc : requiredDocuments) {
            emailContent.append("<li><b>").append(doc).append("</b></li>");
        }
        emailContent.append("</ul>");
        
        emailContent.append("<h3>Détails du dossier:</h3>");
        emailContent.append("<ul>");
        emailContent.append("<li><b>Numéro de dossier:</b> ").append(dossier.getId()).append("</li>");
        
        if (dossier.getReferencesCautions() != null && !dossier.getReferencesCautions().isEmpty()) {
            emailContent.append("<li><b>Référence caution:</b> ").append(dossier.getReferencesCautions()).append("</li>");
        }
        
        if (dossier.getReferencesLC() != null && !dossier.getReferencesLC().isEmpty()) {
            emailContent.append("<li><b>Référence LC:</b> ").append(dossier.getReferencesLC()).append("</li>");
        }
        
        if (dossier.getReferencesCredits() != null && !dossier.getReferencesCredits().isEmpty()) {
            emailContent.append("<li><b>Référence crédit:</b> ").append(dossier.getReferencesCredits()).append("</li>");
        }
        
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
        
        emailContent.append("<p>En tant qu'utilisateur DO, veuillez vous connecter à l'application pour télécharger les documents requis.</p>");
        emailContent.append("<a href='http://localhost:4200/dossiers/").append(dossier.getId())
                .append("' style='background-color: #4CAF50; color: white; padding: 10px 15px; text-align: center; ")
                .append("text-decoration: none; display: inline-block; border-radius: 5px;'>")
                .append("Accéder au dossier</a>");
        
        emailContent.append("<p>Cordialement,<br>Le système de gestion des recouvrements BNM</p>");
        emailContent.append("</body></html>");
        
        return emailContent.toString();
    }

    // New method for rejection notifications
    public void sendRejectionNotification(Long dossierId, String reason, List<String> userTypes) {
        Optional<DossierRecouvrement> dossierOpt = dossierRecouvrementRepository.findById(dossierId);
        if (dossierOpt.isEmpty()) {
            System.err.println("Dossier not found for rejection notification: #" + dossierId);
            return;
        }
        DossierRecouvrement dossier = dossierOpt.get();

        String subject = "Dossier de recouvrement refusé: #" + dossier.getId();
        String notificationTitle = "Dossier refusé: #" + dossier.getId();
        String notificationContent = "Le dossier #" + dossier.getId() + " a été refusé. Motif: " + reason;

        List<User> usersToNotify = new ArrayList<>();
        for (String userType : userTypes) {
            usersToNotify.addAll(userRepository.findByUserType(userType));
        }

        if (usersToNotify.isEmpty()) {
            System.out.println("Aucun utilisateur trouvé pour envoyer la notification de refus pour le dossier #" + dossierId);
            return;
        }

        for (User user : usersToNotify) {
            try {
                emailService.sendEmail(user.getEmail(), subject, buildRejectionEmail(dossier, reason), true);
                Notification notification = new Notification(
                    user,
                    dossier,
                    notificationTitle,
                    notificationContent,
                    "REFUS_DOSSIER"
                );
                notificationRepository.save(notification);
                System.out.println("Notification de refus envoyée à " + user.getEmail() + " pour le dossier #" + dossier.getId());
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de la notification de refus à " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String buildRejectionEmail(DossierRecouvrement dossier, String reason) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<h2>Dossier de recouvrement refusé</h2>");
        emailContent.append("<p>Le dossier de recouvrement suivant a été refusé:</p>");
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
        emailContent.append("<li><b>Motif du refus:</b> ").append(reason).append("</li>");
        emailContent.append("</ul>");
        emailContent.append("<p>Veuillez vous connecter à l'application pour consulter les détails.</p>");
        emailContent.append("<a href='http://localhost:4200/dossiers/").append(dossier.getId())
                .append("' style='background-color: #f44336; color: white; padding: 10px 15px; text-align: center; ")
                .append("text-decoration: none; display: inline-block; border-radius: 5px;'>")
                .append("Accéder au dossier</a>");
        emailContent.append("<p>Cordialement,<br>Le système de gestion des recouvrements BNM</p>");
        emailContent.append("</body></html>");
        return emailContent.toString();
    }
}