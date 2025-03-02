package com.bnm.recouvrement.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.CreditRepository;
import com.bnm.recouvrement.dao.GarantieRepository;
import com.bnm.recouvrement.dto.CreditDTO;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.Credit;
import com.bnm.recouvrement.entity.Garantie;
import com.bnm.recouvrement.service.CreditService;
import com.bnm.recouvrement.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


@RestController
@RequestMapping("/credit")
public class CreditController {

    @Autowired
    private CreditService creditService;
     @Autowired
    private CompteRepository compteRepository;
    @Autowired
    private GarantieRepository garantieRepository;
   
    @Autowired
    private CreditRepository creditRepository;

    // Créer un crédit avec des documents
    @PostMapping(Constants.create)
    @PreAuthorize("hasAuthority('CREATE_CREDIT')")

public ResponseEntity<Credit> creerCredit(@RequestBody CreditDTO creditDTO) throws IOException {
    Compte compte = compteRepository.findById(creditDTO.getIdCompte())
        .orElseThrow(() -> new RuntimeException("Compte introuvable : " + creditDTO.getIdCompte()));
    Garantie garantie = garantieRepository.findById(creditDTO.getIdGarantie())
        .orElseThrow(() -> new RuntimeException("Garantie introuvable : " + creditDTO.getIdGarantie()));

    Credit credit = creditService.toEntity(creditDTO, compte,garantie);
    Credit nouveauCredit = creditService.creerCredit(credit);

    return ResponseEntity.ok(nouveauCredit);
}

    // Lire un crédit par ID
    @GetMapping(Constants.lire+"/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Credit> lireCredit(@PathVariable Long id) {
        return creditService.lireCredit(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Lire tous les crédits
    @GetMapping(Constants.Affichage)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Credit>> lireTousLesCredits() {
        return ResponseEntity.ok(creditService.lireTousLesCredits());
    }

    // Mettre à jour un crédit
    @PutMapping(Constants.update+"/{id}")
    @PreAuthorize("hasAuthority('UPDATE_CREDIT')")

    public ResponseEntity<Credit> mettreAJourCredit(
            @PathVariable Long id,
            @RequestParam("credit") String creditDTOString
            ) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                

                CreditDTO creditDTO = objectMapper.readValue(creditDTOString, CreditDTO.class);
                Compte compte = compteRepository.findById(creditDTO.getIdCompte())
                .orElseThrow(() -> new RuntimeException("Compte introuvable : " + creditDTO.getIdCompte()));
                Garantie garantie = garantieRepository.findById(creditDTO.getIdGarantie())
                .orElseThrow(() -> new RuntimeException("Garantie introuvable : " + creditDTO.getIdGarantie()));


        // Mapper le DTO en entité Credit
                 Credit creditMiseAJour = creditService.toEntity(creditDTO, compte, garantie);

        // Sauvegarder l'entité
                 Credit credit = creditService.mettreAJourCredit(id, creditMiseAJour);

                 return ResponseEntity.ok().build();
                }

    // Supprimer un crédit
    @DeleteMapping(Constants.delete+"/{id}")
    @PreAuthorize("hasAuthority('DELETE_CREDIT')")

    public ResponseEntity<String> supprimerCredit(@PathVariable Long id) {
        creditService.supprimerCredit(id);
        return ResponseEntity.ok("Crédit supprimé avec succès.");
    }
    
    @GetMapping(Constants.telecharger+"/{id}")
    @PreAuthorize("hasAuthority('DOWNLOAD_CREDIT')")
    public ResponseEntity<String> telechargerTousLesDocuments(@PathVariable Long id) {
        String dossierPath = creditService.telechargerTousLesDocuments(id);
        return ResponseEntity.ok("Documents sauvegardés dans le dossier : " + dossierPath);
    }

    @GetMapping(Constants.recherche)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Credit>> rechercherCreditParNomCompte( @RequestParam("nomCompte") String nomCompte) {
        List<Credit> credits = creditService.rechercherParNomCompte(nomCompte);
        if (credits.isEmpty()) {
            return ResponseEntity.noContent().build(); 
        }

        return ResponseEntity.ok(credits); // Retourne la liste des crédits trouvés
    }

   
   @PostMapping(value = "/import-credit")
   @PreAuthorize("hasAuthority('IMPORT_CREDIT')")

public ResponseEntity<Map<String, String>> importCredits(@RequestParam("file") MultipartFile file) {
    Map<String, String> response = new HashMap<>();
    try {
        // Vérifier que le fichier est au format CSV
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".csv")) {
            response.put("error", "Le fichier doit être au format CSV");
            return ResponseEntity.badRequest().body(response);
        }

        // Appeler le service pour importer les crédits
        creditService.importCreditsFromFile(file.getInputStream());

        // Réponse en cas de succès
        response.put("message", "Crédits importés avec succès.");
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        // En cas d'erreur, retourner un message d'erreur détaillé
        response.put("error", "Erreur lors de l'importation des crédits : " + e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
}



