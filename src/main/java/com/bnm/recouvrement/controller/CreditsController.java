package com.bnm.recouvrement.controller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.entity.Agence;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.service.ChequeService;
import com.bnm.recouvrement.service.CreditsService;
import com.bnm.recouvrement.service.DossierRecouvrementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
@RestController
@RequestMapping("/credits")
@CrossOrigin(origins = "http://localhost:4200")
public class CreditsController {

    @Autowired
    private CreditsService creditsService;

@Autowired
private DossierRecouvrementRepository dossierRecouvrementRepository;


    // Uploader un fichier de crédit
    @PostMapping("/{dossierId}/upload")
    public ResponseEntity<DossierRecouvrement> uploadCreditsFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file) throws IOException {
        DossierRecouvrement dossier = creditsService.uploadCreditsFile(dossierId, file);
        return ResponseEntity.ok(dossier);
    }

    // Supprimer un fichier de crédit
    @DeleteMapping("/{dossierId}/delete")
    public ResponseEntity<String> deleteCreditsFile(@PathVariable Long dossierId) {
        try {
            creditsService.deleteCreditsFile(dossierId);
            return ResponseEntity.ok("Fichier de crédit supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression du fichier");
        }
    }

    // Récupérer l'URL du fichier de crédit
    @GetMapping("/{dossierId}/url")
    public ResponseEntity<String> getCreditsFileUrl(@PathVariable Long dossierId) {
        String creditsFileUrl = creditsService.getCreditsFile(dossierId);
        if (creditsFileUrl != null) {
            return ResponseEntity.ok(creditsFileUrl);
        } else {
            return ResponseEntity.status(404).body("Aucun fichier de crédit trouvé");
        }
    }

      private final String basePath = "./uploads/credits/";
   @GetMapping("/api/credits/pdf/{id}")
public ResponseEntity<byte[]> getCreditPdf(@PathVariable Long id) {
    DossierRecouvrement dossier = dossierRecouvrementRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

    String filePath = dossier.getCreditsFile(); // ← exemple : "uploads/credits/credit_3.pdf"
    if (filePath == null || filePath.isBlank()) {
        return ResponseEntity.notFound().build();
    }

    try {
        byte[] pdf = Files.readAllBytes(Paths.get(filePath));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    } catch (IOException e) {
        System.out.println("❌ Erreur lecture fichier crédit : " + e.getMessage());
        return ResponseEntity.notFound().build();
    }
}
}