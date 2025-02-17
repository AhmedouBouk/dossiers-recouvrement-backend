package com.bnm.recouvrement.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.entity.Garantie;
import com.bnm.recouvrement.service.GarantieService;
import com.bnm.recouvrement.utils.Constants;

@RestController
@RequestMapping("/Garantie")
public class GarantieController {
     @Autowired
    private GarantieService garantieService;

    @PostMapping(Constants.create)
@PreAuthorize("hasAnyAuthority('DO', 'DC')")
public ResponseEntity<Garantie> creerGarantie(@RequestBody Garantie garantie) throws IOException {
    Garantie nouvelleGarantie = garantieService.creerGarantie(garantie);
    return ResponseEntity.ok(nouvelleGarantie);
}
    

    // Lire une garantie par son ID
    @GetMapping(Constants.lire+"/{id}")
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<Garantie> lireGarantie(@PathVariable Long id) {
        return garantieService.lireGarantie(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lire toutes les garanties
    @GetMapping(Constants.Affichage)
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<List<Garantie>> lireToutesLesGaranties() {
        List<Garantie> garanties = garantieService.lireToutesLesGaranties();
        return ResponseEntity.ok(garanties);
    }

    // Mettre à jour une garantie
    @PutMapping(Constants.update+"/{id}")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")

    public ResponseEntity<Garantie> mettreAJourGarantie(
            @PathVariable Long id,
            @RequestBody Garantie garantieMiseAJour) {
        Garantie garantie = garantieService.mettreAJourGarantie(id, garantieMiseAJour);
        return ResponseEntity.ok(garantie);
    }

    // Supprimer une garantie
    @DeleteMapping(Constants.delete+"/{id}")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")

    public ResponseEntity<String> supprimerGarantie(@PathVariable Long id) {
        garantieService.supprimerGarantie(id);
        return ResponseEntity.ok("Garantie supprimée avec succès.");
    }
    

    
}