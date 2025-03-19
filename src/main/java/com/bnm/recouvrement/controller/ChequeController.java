package com.bnm.recouvrement.controller;


import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.ChequeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/cheques")
@CrossOrigin(origins = "http://localhost:4200")
public class ChequeController {

    @Autowired
    private ChequeService chequeService;

    // Uploader un fichier de chèque
    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<DossierRecouvrement> uploadChequeFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file) throws IOException {
        DossierRecouvrement dossier = chequeService.uploadChequeFile(dossierId, file);
        return ResponseEntity.ok(dossier);
    }

    // Supprimer un fichier de chèque
    @DeleteMapping("/{dossierId}/delete")
    public ResponseEntity<String> deleteChequeFile(@PathVariable Long dossierId) {
        try {
            chequeService.deleteChequeFile(dossierId);
            return ResponseEntity.ok("Fichier de chèque supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression du fichier");
        }
    }

    // Récupérer l'URL du fichier de chèque
    @GetMapping("/{dossierId}/url")
    public ResponseEntity<String> getChequeFileUrl(@PathVariable Long dossierId) {
        String chequeFileUrl = chequeService.getChequeFile(dossierId);
        if (chequeFileUrl != null) {
            return ResponseEntity.ok(chequeFileUrl);
        } else {
            return ResponseEntity.status(404).body("Aucun fichier de chèque trouvé");
        }
    }
   
    
}