package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.Garantie;
import com.bnm.recouvrement.service.GarantieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grantie")
public class GarantieController {

    @Autowired
    private GarantieService garantieService;
    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;

    // Uploader un fichier de garantie
    @PostMapping("/{dossierId}/garantie")
    public ResponseEntity<DossierRecouvrement> uploadGarantie(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre) throws IOException {
        DossierRecouvrement dossier = garantieService.uploadGarantie(dossierId, file, titre);
        return ResponseEntity.ok(dossier);
    }
    
    // Récupérer toutes les garanties d'un dossier
    @GetMapping("/{dossierId}/garanties")
    public ResponseEntity<List<Garantie>> getGarantiesByDossierId(@PathVariable Long dossierId) {
        List<Garantie> garanties = garantieService.getGarantiesByDossierId(dossierId);
        return ResponseEntity.ok(garanties);
    }

    // Récupérer une garantie par son ID
    @GetMapping("/garantie/{garantieId}")
    public ResponseEntity<Garantie> getGarantieById(@PathVariable Long garantieId) {
        try {
            Garantie garantie = garantieService.getGarantieById(garantieId);
            return ResponseEntity.ok(garantie);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/garantie/{garantieId}")
    public ResponseEntity<Garantie> updateGarantie(
            @PathVariable Long garantieId,
            @RequestParam("titre") String nouveauTitre) {
        Garantie updatedGarantie = garantieService.updateGarantie(garantieId, nouveauTitre);
        return ResponseEntity.ok(updatedGarantie);
    }
    // Supprimer un fichier de garantie
    @DeleteMapping("/garantie/{garantieId}")
    public ResponseEntity<Map<String, String>> deleteGarantie(@PathVariable Long garantieId) {
        try {
            garantieService.deleteGarantie(garantieId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Garantie supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la suppression de la garantie");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/{dossierId}/garanties/delete-all")
    public ResponseEntity<Map<String, String>> deleteAllGaranties(@PathVariable Long dossierId) {
        try {
            garantieService.deleteAllGarantiesByDossierId(dossierId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Toutes les garanties ont été supprimées avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la suppression des garanties");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/garantie/{garantieId}/pdf")
    public ResponseEntity<byte[]> getGarantiePdf(@PathVariable Long garantieId) {
        try {
            byte[] pdf = garantieService.getGarantiePdf(garantieId);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            System.out.println("Erreur lecture fichier garantie : " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
@GetMapping("/api/debug/dossier/{id}")
public DossierRecouvrement testDossier(@PathVariable Long id) {
    return dossierRecouvrementRepository.findById(id).orElseThrow();
}
}