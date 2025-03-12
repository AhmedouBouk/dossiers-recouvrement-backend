package com.bnm.recouvrement.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.DossierRecouvrementService;
import org.springframework.core.io.Resource;
@RestController
@RequestMapping("/dossiers")
@CrossOrigin(origins = "http://localhost:4200")
public class DossierRecouvrementController {

    @Autowired
    private DossierRecouvrementService dossierService;

    @GetMapping("/affichage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DossierRecouvrement>> getAllDossiers() {
        try {
            List<DossierRecouvrement> dossiers = dossierService.getAllDossiers();
            return ResponseEntity.ok(dossiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> getDossierById(@PathVariable Long id) {
        Optional<DossierRecouvrement> dossier = dossierService.getDossierById(id);
        return dossier.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> updateDossier(
            @PathVariable Long id,
            @RequestBody DossierRecouvrement dossier) {
        try {
            DossierRecouvrement updatedDossier = dossierService.updateDossier(id, dossier);
            return ResponseEntity.ok(updatedDossier);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteDossier(@PathVariable Long id) {
        try {
            dossierService.deleteDossier(id);
            return ResponseEntity.ok("Dossier supprimé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recherche")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DossierRecouvrement>> searchDossiers(
            @RequestParam(required = false) Long dossierId,
            @RequestParam(required = false) String numeroCompte,
            @RequestParam(required = false) String nomClient) {
        try {
            List<DossierRecouvrement> dossiers = dossierService.searchDossiers(dossierId, numeroCompte, nomClient);
            if (!dossiers.isEmpty()) {
                return ResponseEntity.ok(dossiers);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> createDossier(@RequestBody DossierRecouvrement dossier) {
        try {
            DossierRecouvrement newDossier = dossierService.createDossier(dossier);
            return ResponseEntity.status(HttpStatus.CREATED).body(newDossier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> importDossiers(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Fichier vide", "status", "error"));
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".csv")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le fichier doit être au format CSV", "status", "error"));
            }

            String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            File tempFile = new File(filePath);
            file.transferTo(tempFile);

            int importCount = dossierService.importDossiersFromFile(filePath);
            tempFile.delete();

            return ResponseEntity.ok(Map.of(
                "message", "Import réussi",
                "count", importCount,
                "status", "success"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "message", "Erreur d'importation: " + e.getMessage(),
                    "status", "error"
                ));
        }
    }
    
    @GetMapping("/{id}/generate-pdf")
    public ResponseEntity<Resource> generateMiseEnDemeure(@PathVariable Long id) {
        return dossierService.generateMiseEnDemeurePdf(id);
    }
}
