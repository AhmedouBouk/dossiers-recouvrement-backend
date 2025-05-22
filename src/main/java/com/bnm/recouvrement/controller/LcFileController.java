package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.LcFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;   
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
@RestController
@RequestMapping("/lc-files")
@CrossOrigin(origins = "http://localhost:4200")
public class LcFileController {

    @Autowired
    private LcFileService lcFileService;
    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;

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
  private final String basePath = "./uploads/lc-files/";

  

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getLcFilePdf(@PathVariable Long id) {
        DossierRecouvrement dossier = dossierRecouvrementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        // Ici, dossier.getLcFile() doit être **uniquement** le nom du fichier
        String filename = dossier.getLcFile();
        if (filename == null || filename.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Résolution du chemin complet
            Path path = Paths.get(basePath).resolve(filename).normalize().toAbsolutePath();
            if (!Files.exists(path)) {
                System.err.println("🔍 Fichier introuvable : " + path);
                return ResponseEntity.notFound().build();
            }

            byte[] pdf = Files.readAllBytes(path);
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(pdf);

        } catch (IOException e) {
            System.err.println("❌ Erreur lecture LC file (" + filename + ") : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}