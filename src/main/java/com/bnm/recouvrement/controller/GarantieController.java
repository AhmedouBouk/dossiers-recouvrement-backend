package com.bnm.recouvrement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.GarantieService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/dossiers")
public class GarantieController {

    @Autowired
    private GarantieService garantieService;

    @GetMapping("/sans-garantie")
    public ResponseEntity<List<DossierRecouvrement>> getDossiersSansGarantie() {
        List<DossierRecouvrement> dossiers = garantieService.getDossiersSansGarantie();
        return ResponseEntity.ok(dossiers);
    }

    @PostMapping("/{dossierId}/garantie")
    public ResponseEntity<DossierRecouvrement> uploadGarantie(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre) throws IOException {
        DossierRecouvrement dossier = garantieService.uploadGarantie(dossierId, file, titre);
        return ResponseEntity.ok(dossier);
    }
}