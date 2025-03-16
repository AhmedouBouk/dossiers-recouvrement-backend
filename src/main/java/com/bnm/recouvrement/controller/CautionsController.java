package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.CautionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/cautions")
@CrossOrigin(origins = "http://localhost:4200")
public class CautionsController {

    @Autowired
    private CautionsService cautionsService;

    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<DossierRecouvrement> uploadCautionsFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file) throws IOException {
        DossierRecouvrement dossier = cautionsService.uploadCautionsFile(dossierId, file);
        return ResponseEntity.ok(dossier);
    }

    @DeleteMapping("/{dossierId}/delete")
    public ResponseEntity<String> deleteCautionsFile(@PathVariable Long dossierId) {
        cautionsService.deleteCautionsFile(dossierId);
        return ResponseEntity.ok("Fichier de caution supprimé avec succès");
    }

    @GetMapping("/{dossierId}/url")
    public ResponseEntity<String> getCautionsFileUrl(@PathVariable Long dossierId) {
        String cautionsFileUrl = cautionsService.getCautionsFile(dossierId);
        if (cautionsFileUrl != null) {
            return ResponseEntity.ok(cautionsFileUrl);
        } else {
            return ResponseEntity.status(404).body("Aucun fichier de caution trouvé");
        }
    }
}