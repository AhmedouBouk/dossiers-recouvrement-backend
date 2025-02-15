package com.bnm.recouvrement.controller;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.dto.CompteDTO;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.service.CompteService;

@RestController
@RequestMapping("/comptes")
@CrossOrigin(origins = "http://localhost:4200")
public class CompteController {

    @Autowired
    private CompteService compteService;

    @PostMapping("/import-comptes")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public ResponseEntity<?> importComptes(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Fichier vide", "status", "error"));
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            File tempFile = new File(filePath);
            
            file.transferTo(tempFile);
            int importCount = compteService.importComptesFromFile(filePath);
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

    @GetMapping("/Affichage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CompteDTO>> getAllComptesWithClients() {
        try {
            List<CompteDTO> comptesWithClients = compteService.getAllComptesWithClients();
            return ResponseEntity.ok(comptesWithClients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/update/{nomCompte}")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public ResponseEntity<String> mettreAJourCompte(
            @PathVariable String nomCompte,
            @RequestParam(required = false) Double solde,
            @RequestParam(required = false) String etat) {
        try {
            compteService.mettreAJourCompte(nomCompte, solde, etat);
            return ResponseEntity.ok("Compte mis à jour avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{nomCompte}")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public ResponseEntity<String> supprimerCompte(@PathVariable String nomCompte) {
        try {
            compteService.supprimerCompte(nomCompte);
            return ResponseEntity.ok("Compte supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recherche")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<List<CompteDTO>> rechercherComptesAvecDTO(
        @RequestParam(required = false) String nomCompte,
        @RequestParam(required = false) String nom,
        @RequestParam(required = false) String prenom,
        @RequestParam(required = false) Integer nni) {
    try {
        List<CompteDTO> comptes = compteService.rechercherComptesAvecDTO(nomCompte, nom, prenom, nni);
        if (!comptes.isEmpty()) {
            return ResponseEntity.ok(comptes);
        }
        return ResponseEntity.noContent().build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    @GetMapping("/{nomCompte}")
public ResponseEntity<Compte> getCompteByNomCompte(@PathVariable String nomCompte) {
    Optional<Compte> compteOpt = compteService.getCompteByNomCompte(nomCompte);
    if (compteOpt.isPresent()) {
        return ResponseEntity.ok(compteOpt.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

}