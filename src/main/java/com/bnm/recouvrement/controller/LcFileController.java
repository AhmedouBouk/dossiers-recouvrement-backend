package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.LcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/lc-files")
@CrossOrigin(origins = "http://localhost:4200")
public class LcFileController {

    @Autowired
    private LcFileService lcFileService;

    // Uploader un fichier LC
    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<DossierRecouvrement> uploadLcFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file) throws IOException {
        DossierRecouvrement dossier = lcFileService.uploadLcFile(dossierId, file);
        return ResponseEntity.ok(dossier);
    }

    // Supprimer un fichier LC
    @DeleteMapping("/{dossierId}/delete")
    public ResponseEntity<String> deleteLcFile(@PathVariable Long dossierId) {
        lcFileService.deleteLcFile(dossierId);
        return ResponseEntity.ok("Fichier LC supprimé avec succès");
    }

    // Récupérer l'URL du fichier LC
    @GetMapping("/{dossierId}/url")
    public ResponseEntity<String> getLcFileUrl(@PathVariable Long dossierId) {
        String lcFileUrl = lcFileService.getLcFile(dossierId);
        if (lcFileUrl != null) {
            return ResponseEntity.ok(lcFileUrl);
        } else {
            return ResponseEntity.status(404).body("Aucun fichier LC trouvé");
        }
    }
}