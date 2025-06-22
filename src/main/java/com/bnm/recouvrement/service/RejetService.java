package com.bnm.recouvrement.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dto.RejetRequest;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.DossierRecouvrement.EtatValidation;
import com.bnm.recouvrement.entity.Notification;
import com.bnm.recouvrement.entity.Rejet;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.repository.NotificationRepository;
import com.bnm.recouvrement.repository.RejetRepository;
import com.bnm.recouvrement.dao.UserRepository;

import jakarta.mail.MessagingException;

@Service
public class RejetService {

    @Autowired
    private RejetRepository rejetRepository;
    
    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Crée un nouveau rejet et envoie des notifications aux utilisateurs sélectionnés
     * @param dossierId ID du dossier à rejeter
     * @param rejetRequest Données du rejet (motif, types d'utilisateurs, IDs des utilisateurs)
     * @return Le rejet créé
     */
    @Transactional
    public Rejet rejeterDossier(Long dossierId, RejetRequest rejetRequest) {
        // Récupérer le dossier
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté non trouvé"));
        
        // Récupérer les utilisateurs à notifier
        List<User> utilisateursANotifier = new ArrayList<>();
        if (rejetRequest.getUtilisateursIds() != null && !rejetRequest.getUtilisateursIds().isEmpty()) {
            utilisateursANotifier = userRepository.findAllById(rejetRequest.getUtilisateursIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList()));
        }
        
        // Créer le rejet
        Rejet rejet = new Rejet();
        rejet.setDossier(dossier);
        rejet.setMotif(rejetRequest.getMotif());
        rejet.setRejetePar(currentUser);
        rejet.setTypesUtilisateurs(rejetRequest.getTypesUtilisateurs());
        rejet.setUtilisateursNotifies(utilisateursANotifier);
        
        // Mettre à jour le statut du dossier
        dossier.setEtatValidation(EtatValidation.NON_VALIDE);
        dossierRepository.save(dossier);
        
        // Sauvegarder le rejet
        Rejet rejetSauvegarde = rejetRepository.save(rejet);
        
        // Envoyer des notifications aux utilisateurs sélectionnés
        envoyerNotifications(rejetSauvegarde);
        
        return rejetSauvegarde;
    }
    
    /**
     * Récupère tous les rejets pour un dossier spécifique
     * @param dossierId ID du dossier
     * @return Liste des rejets pour ce dossier
     */
    public List<Rejet> getRejetsParDossier(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID: " + dossierId));
        
        return rejetRepository.findByDossierOrderByDateRejetDesc(dossier);
    }
    
    /**
     * Marque un rejet comme traité
     * @param rejetId ID du rejet
     * @return Le rejet mis à jour
     */
    @Transactional
    public Rejet marquerCommeTraite(Long rejetId) {
        Rejet rejet = rejetRepository.findById(rejetId)
                .orElseThrow(() -> new RuntimeException("Rejet non trouvé avec l'ID: " + rejetId));
        
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté non trouvé"));
        
        rejet.setTraite(true);
        rejet.setDateTraitement(LocalDateTime.now());
        rejet.setTraitePar(currentUser);
        
        return rejetRepository.save(rejet);
    }
    
    /**
     * Envoie des notifications aux utilisateurs sélectionnés
     * @param rejet Le rejet concerné
     */
    private void envoyerNotifications(Rejet rejet) {
        String subject = "Dossier de recouvrement refusé: #" + rejet.getDossier().getId();
        String notificationTitle = "Dossier refusé: #" + rejet.getDossier().getId();
        String notificationContent = "Le dossier #" + rejet.getDossier().getId() + " a été refusé. Motif: " + rejet.getMotif();
        
        // Envoyer des notifications aux utilisateurs sélectionnés
        for (User user : rejet.getUtilisateursNotifies()) {
            try {
                // Envoyer un email
                emailService.sendEmail(user.getEmail(), subject, buildRejectionEmail(rejet), true);
                
                // Créer une notification dans le système
                Notification notification = new Notification(
                    user,
                    rejet.getDossier(),
                    notificationTitle,
                    notificationContent,
                    "REFUS_DOSSIER"
                );
                notificationRepository.save(notification);
                
                System.out.println("Notification de refus envoyée à " + user.getEmail() + " pour le dossier #" + rejet.getDossier().getId());
            } catch (MessagingException e) {
                System.err.println("Erreur lors de l'envoi de la notification de refus à " + user.getEmail() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Construit le contenu HTML de l'email de notification de rejet
     * @param rejet Le rejet concerné
     * @return Le contenu HTML de l'email
     */
    private String buildRejectionEmail(Rejet rejet) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html><body>");
        emailContent.append("<h2>Dossier de recouvrement refusé</h2>");
        emailContent.append("<p>Le dossier de recouvrement suivant a été refusé:</p>");
        emailContent.append("<h3>Détails du dossier:</h3>");
        emailContent.append("<ul>");
        emailContent.append("<li><b>Numéro de dossier:</b> ").append(rejet.getDossier().getId()).append("</li>");
        
        if (rejet.getDossier().getCompte() != null) {
            emailContent.append("<li><b>Compte:</b> ").append(rejet.getDossier().getCompte().getNomCompte()).append("</li>");
            
            if (rejet.getDossier().getCompte().getClient() != null) {
                emailContent.append("<li><b>Client:</b> ").append(rejet.getDossier().getCompte().getClient().getNom())
                        .append(" ").append(rejet.getDossier().getCompte().getClient().getPrenom()).append("</li>");
            }
        }
        
        emailContent.append("<li><b>Motif du refus:</b> ").append(rejet.getMotif()).append("</li>");
        emailContent.append("<li><b>Date du refus:</b> ").append(rejet.getDateRejet()).append("</li>");
        emailContent.append("<li><b>Refusé par:</b> ").append(rejet.getRejetePar().getName()).append("</li>");
        emailContent.append("</ul>");
        
        emailContent.append("<p>Veuillez vous connecter à l'application pour consulter les détails.</p>");
        emailContent.append("<a href='http://localhost:4200/dossiers/").append(rejet.getDossier().getId())
                .append("' style='background-color: #f44336; color: white; padding: 10px 15px; text-align: center; ")
                .append("text-decoration: none; display: inline-block; border-radius: 5px;'>")
                .append("Accéder au dossier</a>");
        
        emailContent.append("<p>Cordialement,<br>Le système de gestion des recouvrements BNM</p>");
        emailContent.append("</body></html>");
        
        return emailContent.toString();
    }
    
    /**
     * Récupère tous les rejets
     * @return Liste de tous les rejets
     */
    public List<Rejet> getAllRejets() {
        return rejetRepository.findAll();
    }
    
    /**
     * Récupère tous les rejets où l'utilisateur connecté est notifié
     * @param email Email de l'utilisateur connecté
     * @return Liste des rejets pour cet utilisateur
     */
    public List<Rejet> getRejetsPourUtilisateurConnecte(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return rejetRepository.findByUtilisateursNotifiesContainingOrderByDateRejetDesc(user);
    }
}
