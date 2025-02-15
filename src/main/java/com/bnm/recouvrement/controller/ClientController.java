package com.bnm.recouvrement.controller;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.http.parser.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.utils.Constants;
import com.bnm.recouvrement.dto.ClientDTO;
import com.bnm.recouvrement.entity.Client;
import com.bnm.recouvrement.service.ClientService;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:4200") // This allows access from any origin
public class ClientController {
    @Autowired
    private ClientService clientService;
    

    @GetMapping("/Affichage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Client>> getAllClients() {
        try {
            List<Client> clients = clientService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping(value = "/import-client")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public ResponseEntity<String> importClients(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Le fichier doit être au format CSV");
            }

            String filePath = System.getProperty("java.io.tmpdir") + "/" + originalFilename;
            File tempFile = new File(filePath);
            file.transferTo(tempFile);

            clientService.importClientsFromFile(filePath);

            tempFile.delete();

            return ResponseEntity.ok("Clients importés avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("Erreur lors de l'importation des clients : " + e.getMessage());
        }
    }


    @PutMapping("/update/{nni}")
    @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public String mettreAJourClient(
            @PathVariable Integer nni,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) Double salaire,
            @RequestParam(required = false) String secteurActivite) {

        clientService.mettreAJourClient(nni, nom, prenom, adresse, salaire, secteurActivite);
        return "Le client avec le NNI " + nni + " a été mis à jour avec succès.";
    }

    @DeleteMapping("/delete/{nni}")
    // @PreAuthorize("hasAnyAuthority('DO', 'DC')")
    public String supprimerClient(@PathVariable Integer nni) {
        clientService.supprimerClient(nni);
        return "Le client avec le NNI " + nni + " a été supprimé avec succès.";
    }
  
    @GetMapping("/recherche")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Client>> rechercherClients(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) Integer nni) {
        List<Client> clients = clientService.rechercherClients(nom, prenom, nni);
    
        if (!clients.isEmpty()) {
            return ResponseEntity.ok(clients);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    @GetMapping("/{nni}")
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<ClientDTO> getClientByNni(@PathVariable Integer nni) {
        Optional<Client> client = clientService.getClientByNni(nni);
        if (client.isPresent()) {
            return ResponseEntity.ok(new ClientDTO(client.get()));
        }
        return ResponseEntity.notFound().build();
    }
}