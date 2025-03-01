package com.bnm.recouvrement.controller;

import java.io.File;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.DossierRecouvrementService;
import com.bnm.recouvrement.utils.Constants;


@RestController
@RequestMapping("/DossierRecouvrement")
public class DossierRecouvrementController {
      @Autowired
    private DossierRecouvrementService dossierRecouvrementService;

    @PostMapping("/detection-impayes")
    @PreAuthorize("hasAuthority('DETECT_IMPAYES')")

    public ResponseEntity<String> detecterImpayes(@RequestParam("file") MultipartFile file) {
        try {
            dossierRecouvrementService.detecterImpayesEtCreerDossiers(file);
            return ResponseEntity.ok("Fichier traité avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }
    @PostMapping("/upload/{dossierId}/ajouter-credit/{creditId}")
    @PreAuthorize("hasAuthority('ADD_CREDIT_TO_DOSSIER')")
    public ResponseEntity<DossierRecouvrement> ajouterCredit(
            @PathVariable Long dossierId, @PathVariable Long creditId) {
        try {
            DossierRecouvrement dossier = dossierRecouvrementService.ajouterCredit(dossierId, creditId);
            return ResponseEntity.ok(dossier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Modifier un dossier de recouvrement
    @PutMapping(Constants.update+"/{dossierId}")
    @PreAuthorize("hasAuthority('UPDATE_DOSSIER')")

    public ResponseEntity<DossierRecouvrement> modifierDossier(
            @PathVariable Long dossierId, @RequestBody DossierRecouvrement modifications) {
        DossierRecouvrement dossier = dossierRecouvrementService.modifierDossier(dossierId, modifications);
        return ResponseEntity.ok(dossier);
    }

    // Modifier le statut d'un dossier
    @PatchMapping("/{dossierId}/modifier-statut")
    @PreAuthorize("hasAuthority('MODIFY_DOSSIER_STATUS')")


    public ResponseEntity<DossierRecouvrement> modifierStatut(
            @PathVariable Long dossierId, @RequestParam String nouveauStatut) {
        DossierRecouvrement dossier = dossierRecouvrementService.modifierStatut(dossierId, nouveauStatut);
        return ResponseEntity.ok(dossier);
    }

    // Supprimer un dossier de recouvrement
    @DeleteMapping(Constants.delete+"/{dossierId}")
    @PreAuthorize("hasAuthority('DELETE_DOSSIER')")


    public ResponseEntity<String> supprimerDossier(@PathVariable Long dossierId) {
        dossierRecouvrementService.supprimerDossier(dossierId);
        return ResponseEntity.ok().build();
    }

    // Lire un dossier par ID
    @GetMapping(Constants.lire+"/{dossierId}")
        @PreAuthorize("isAuthenticated()")

    public ResponseEntity<DossierRecouvrement> lireDossier(@PathVariable Long dossierId) {
        return dossierRecouvrementService.lireDossier(dossierId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Afficher tous les dossiers
    @GetMapping(Constants.Affichage)
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<List<DossierRecouvrement>> afficherTousLesDossiers() {
        List<DossierRecouvrement> dossiers = dossierRecouvrementService.afficherTousLesDossiers();
        return ResponseEntity.ok(dossiers);
    }
    @GetMapping(Constants.telecharger+"/{dossierId}")
    @PreAuthorize("hasAuthority('DOWNLOAD_DOSSIER')")

    public ResponseEntity<String> sauvegarderDossier(@PathVariable Long dossierId) {
        String cheminDossier = "C:\\Users\\DELL\\Desktop\\IRT11"; // Chemin absolu ou relatif
        new File(cheminDossier).mkdirs(); // Crée le répertoire s'il n'existe pas
    
        try {
            dossierRecouvrementService.sauvegarderDossierDansUnFichier(dossierId, cheminDossier);
            return ResponseEntity.ok("Fichier ZIP créé avec succès dans le dossier : " + cheminDossier);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la création du fichier ZIP : " + e.getMessage());
        }
    }
    // Rechercher un dossier par accountNumber
    @GetMapping(Constants.recherche)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> rechercherDossierParAccountNumber(
            @RequestParam String accountNumber) {
        return dossierRecouvrementService.rechercherParAccountNumberExact(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // Retourne 404 si aucun dossier trouvé
    }
    

    
    
}
