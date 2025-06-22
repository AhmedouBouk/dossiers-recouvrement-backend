package com.bnm.recouvrement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bnm.recouvrement.dto.RejetRequest;
import com.bnm.recouvrement.entity.Rejet;
import com.bnm.recouvrement.service.RejetService;

@RestController
@RequestMapping("/rejets")
public class RejetController {

    @Autowired
    private RejetService rejetService;
    
    /**
     * Crée un nouveau rejet pour un dossier
     * @param dossierId ID du dossier à rejeter
     * @param rejetRequest Données du rejet
     * @return Le rejet créé
     */
    @PostMapping("/dossiers/{dossierId}")
    public ResponseEntity<Rejet> rejeterDossier(
            @PathVariable Long dossierId,
            @RequestBody RejetRequest rejetRequest) {
        try {
            Rejet rejet = rejetService.rejeterDossier(dossierId, rejetRequest);
            return ResponseEntity.ok(rejet);
        } catch (Exception e) {
            System.err.println("Erreur lors du rejet du dossier: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère tous les rejets pour un dossier
     * @param dossierId ID du dossier
     * @return Liste des rejets pour ce dossier
     */
    @GetMapping("/dossiers/{dossierId}")
    public ResponseEntity<List<Rejet>> getRejetsParDossier(@PathVariable Long dossierId) {
        try {
            List<Rejet> rejets = rejetService.getRejetsParDossier(dossierId);
            return ResponseEntity.ok(rejets);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des rejets: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Marque un rejet comme traité
     * @param rejetId ID du rejet
     * @return Le rejet mis à jour
     */
    @PostMapping("/{rejetId}/traiter")
    public ResponseEntity<Rejet> marquerCommeTraite(@PathVariable Long rejetId) {
        try {
            Rejet rejet = rejetService.marquerCommeTraite(rejetId);
            return ResponseEntity.ok(rejet);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du rejet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère tous les rejets
     * @return Liste de tous les rejets
     */
    @GetMapping
    public ResponseEntity<List<Rejet>> getAllRejets() {
        try {
            List<Rejet> rejets = rejetService.getAllRejets();
            return ResponseEntity.ok(rejets);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de tous les rejets: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Récupère tous les rejets où l'utilisateur connecté est notifié
     * @return Liste des rejets pour l'utilisateur connecté
     */
    @GetMapping("/mes-rejets")
    public ResponseEntity<List<Rejet>> getRejetsPourUtilisateurConnecte() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            List<Rejet> rejets = rejetService.getRejetsPourUtilisateurConnecte(email);
            return ResponseEntity.ok(rejets);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des rejets pour l'utilisateur connecté: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
