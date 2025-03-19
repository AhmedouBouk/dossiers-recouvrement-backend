package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dto.ClientDTO;
import com.bnm.recouvrement.dto.CompteDTO;
import com.bnm.recouvrement.dao.ClientRepository;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompteService {

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private HistoryService historyService;

    @Transactional
    public int importComptesFromFile(String filePath) throws Exception {
        int importedCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
    
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
    
                try {
                    String[] data = line.trim().split(",");
                    if (data.length < 6) {
                        throw new Exception("Format de ligne invalide: " + line);
                    }

                    // Nettoyage des données
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i].trim();
                    }

                    Integer nni = Integer.valueOf(data[0]);
                    Client client = clientRepository.findByNni(nni)
                        .orElseThrow(() -> new Exception("Client non trouvé avec NNI: " + nni));

                    String nomCompte = data[1];
                    String libelecategorie = data[2];
                    Double solde = Double.parseDouble(data[3]);
                    String etat = data[4];
                    LocalDate dateOuverture = LocalDate.parse(data[5]);
                    int categorie = Integer.parseInt(data[6]);
                    Compte compte = compteRepository.findById(nomCompte)
                        .orElse(new Compte(client, nomCompte, libelecategorie, categorie, solde, etat, dateOuverture));

                    if (compteRepository.existsById(nomCompte)) {
                        compte.setSolde(solde);
                        compte.setEtat(etat);
                    }

                    compteRepository.save(compte);
                    importedCount++;

                } catch (Exception e) {
                    System.err.println("Erreur ligne " + line + ": " + e.getMessage());
                    throw new Exception("Erreur à la ligne: " + line + " - " + e.getMessage());
                }
            }
            
            // Enregistrer l'événement d'import dans l'historique
            if (importedCount > 0) {
                // Récupérer l'utilisateur actuel
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                
                // Créer un événement d'historique
                historyService.logImport(
                    username, 
                    new File(filePath).getName(), 
                    "Import de " + importedCount + " comptes réussi"
                );
                
                System.out.println("Historique d'import créé pour " + importedCount + " comptes par " + username);
            }
        }
    
        return importedCount;
    }

    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }
   
    @Transactional
    public void mettreAJourCompte(String nomCompte, Double nouveauSolde, String nouvelEtat) {
        Compte compte = compteRepository.findById(nomCompte)
            .orElseThrow(() -> new IllegalArgumentException("Compte non trouvé: " + nomCompte));

        if (nouveauSolde != null) {
            compte.setSolde(nouveauSolde);
        }
        if (nouvelEtat != null && !nouvelEtat.isBlank()) {
            compte.setEtat(nouvelEtat);
        }

        compteRepository.save(compte);
        
        // Enregistrer l'événement de mise à jour dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logUpdate(
            username, 
            "compte", 
            nomCompte, 
            "Compte " + nomCompte
        );
    }

    @Transactional
    public void supprimerCompte(String nomCompte) {
        if (!compteRepository.existsById(nomCompte)) {
            throw new IllegalArgumentException("Compte non trouvé: " + nomCompte);
        }
        
        // Enregistrer l'événement de suppression dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logDelete(
            username, 
            "compte", 
            nomCompte, 
            "Compte " + nomCompte
        );
        
        compteRepository.deleteById(nomCompte);
    }

    public List<CompteDTO> rechercherComptesAvecDTO(String nomCompte, String nom, String prenom, Integer nni) {
        List<Compte> comptes;
    
        if (nomCompte != null) {
            comptes = compteRepository.findByNomCompte(nomCompte)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else if (nom != null && prenom != null && nni != null) {
            comptes = compteRepository.findByClientNomContainingIgnoreCaseAndClientPrenomContainingIgnoreCaseAndClientNni(
                    nom, prenom, nni);
        } else if (nom != null && prenom != null) {
            comptes = compteRepository.findByClientNomContainingIgnoreCaseAndClientPrenomContainingIgnoreCase(nom, prenom);
        } else if (nom != null) {
            comptes = compteRepository.findByClientNomContainingIgnoreCase(nom);
        } else if (prenom != null) {
            comptes = compteRepository.findByClientPrenomContainingIgnoreCase(prenom);
        } else if (nni != null) {
            comptes = compteRepository.findByClientNni(nni);
        } else {
            comptes = Collections.emptyList();
        }
    
        return comptes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<Compte> getCompteByNomCompte(String nomCompte) {
        return compteRepository.findById(nomCompte);
    }
    
    public List<CompteDTO> getAllComptesWithClients() {
        List<Compte> comptes = compteRepository.findAll();
        return comptes.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private CompteDTO convertToDTO(Compte compte) {
        CompteDTO compteDTO = new CompteDTO();
        compteDTO.setNomCompte(compte.getNomCompte());
        compteDTO.setLibelecategorie(compte.getLibelecategorie());
        compteDTO.setCategorie(compte.getCategorie());
        compteDTO.setSolde(compte.getSolde());
        compteDTO.setEtat(compte.getEtat());
        compteDTO.setDateOuverture(compte.getDateOuverture());
        
        if (compte.getClient() != null) {
            ClientDTO clientDTO = new ClientDTO(
                compte.getClient().getNni(),
                compte.getClient().getNif(),
                compte.getClient().getNom(),
                compte.getClient().getPrenom(),
                compte.getClient().getDateNaissance(),
                compte.getClient().getSecteurActivite(),
                compte.getClient().getGenre(),
                compte.getClient().getSalaire(),
                compte.getClient().getAdresse()
            );
            compteDTO.setClient(clientDTO);
        }
        
        return compteDTO;
    }
}