package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.GarantieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/grantie")
public class GarantieController {

    @Autowired
    private GarantieService garantieService;

    // Uploader un fichier de garantie
    @PostMapping("/{dossierId}/garantie")
    public ResponseEntity<DossierRecouvrement> uploadGarantie(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre) throws IOException {
        DossierRecouvrement dossier = garantieService.uploadGarantie(dossierId, file, titre);
        return ResponseEntity.ok(dossier);
    }

    // Récupérer l'URL du fichier de garantie
    @GetMapping("/{dossierId}/garantie/url")
    public ResponseEntity<String> getGarantieFileUrl(@PathVariable Long dossierId) {
        String garantieFileUrl = garantieService.getGarantieFileUrl(dossierId);
        if (garantieFileUrl != null) {
            return ResponseEntity.ok(garantieFileUrl);
        } else {
            return ResponseEntity.status(404).body("Aucun fichier de garantie trouvé");
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<DossierRecouvrement> updateGarantie(
            @PathVariable Long id,
            @RequestBody DossierRecouvrement garantieDetails) {
        DossierRecouvrement updatedGarantie = garantieService.updateGarantie(id, garantieDetails);
        return ResponseEntity.ok(updatedGarantie);
    }
    // Supprimer un fichier de garantie
  @DeleteMapping("/{dossierId}/garantie/delete")
public ResponseEntity<Map<String, String>> deleteGarantieFile(@PathVariable Long dossierId) {
    try {
        garantieService.deleteGarantieFile(dossierId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Fichier de garantie supprimé avec succès");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Erreur lors de la suppression du fichier");
        return ResponseEntity.status(500).body(response);
    }

}}