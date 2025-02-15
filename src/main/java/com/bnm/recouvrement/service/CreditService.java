package com.bnm.recouvrement.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.CreditRepository;
import com.bnm.recouvrement.dto.CreditDTO;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.Credit;

@Service
public class CreditService {
 @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private CreditRepository creditRepository;
    
    // Créer un crédit avec des documents
    public Credit creerCredit(Credit credit) throws IOException {

        return creditRepository.save(credit);
    }

    // Lire un crédit par ID
    public Optional<Credit> lireCredit(Long id) {
        return creditRepository.findById(id);
    }

    // Lire tous les crédits
    public List<Credit> lireTousLesCredits() {
        return creditRepository.findAll();
    }

    // Mettre à jour un crédit existant
    public Credit mettreAJourCredit(Long id, Credit creditMiseAJour) throws IOException {
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crédit non trouvé pour l'id : " + id));
    
        credit.setMontant(creditMiseAJour.getMontant());
        credit.setTauxInteret(creditMiseAJour.getTauxInteret());
        credit.setDuree(creditMiseAJour.getDuree());
        credit.setDateDebut(creditMiseAJour.getDateDebut());
        credit.setStatut(creditMiseAJour.getStatut());
        credit.setRefTransaction(creditMiseAJour.getRefTransaction());
        credit.setTypeGarantie(creditMiseAJour.getTypeGarantie());      // Add this line
        credit.setValeurGarantie(creditMiseAJour.getValeurGarantie());
        credit.setFondDossier(creditMiseAJour.getFondDossier());  // Add this line
        credit.setCompte(creditMiseAJour.getCompte());           // Add this line
        return creditRepository.save(credit);
    }

    // Supprimer un crédit
    public void supprimerCredit(Long id) {
        if (!creditRepository.existsById(id)) {
            throw new RuntimeException("Crédit non trouvé pour l'id : " + id);
        }
        creditRepository.deleteById(id);
    }
     public String telechargerTousLesDocuments(Long id) {
        Credit credit = creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crédit non trouvé pour l'id : " + id));

        // Créer un dossier pour le crédit
        String dossierPath = "credits/" + id + "/";
        File dossier = new File(dossierPath);
        if (!dossier.exists() && !dossier.mkdirs()) {
            throw new RuntimeException("Impossible de créer le dossier : " + dossierPath);
        }

        // Sauvegarder chaque document
        

        return dossierPath;
    }

    
    
    public Credit toEntity(CreditDTO creditDTO, Compte compte) {
        Credit credit = new Credit();
        credit.setMontant(creditDTO.getMontant());
        credit.setTauxInteret(creditDTO.getTauxInteret());
        credit.setDuree(creditDTO.getDuree());
        credit.setDateDebut(creditDTO.getDateDebut());
        credit.setStatut(creditDTO.getStatut());
        credit.setRefTransaction(creditDTO.getRefTransaction());
        credit.setTypeGarantie(creditDTO.getTypeGarantie());
        credit.setValeurGarantie(creditDTO.getValeurGarantie());
        credit.setFondDossier(creditDTO.getFondDossier());  // Add this line
        credit.setCompte(compte);
        return credit;
    }
    public List<Credit> rechercherParNomCompte(String nomCompte) {
        return creditRepository.findByCompteNomCompte(nomCompte);
    }
    

    

    @Transactional
    public void importCreditsFromFile(InputStream inputStream) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean firstLine = true;
    
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }
    
                String[] data = line.split(",");
                try {
                    // Valider les données
                    if (data.length < 3) {
                        throw new Exception("La ligne CSV doit contenir au moins 3 colonnes : idCredit, fileLink, numeroCompte");
                    }
    
                    Long idCredit = Long.parseLong(data[0].trim());
                    String fileLink = data[1].trim();
                    String numeroCompte = data[2].trim();
    
                    // Vérifier si le compte existe
                    Compte compte = compteRepository.findByNomCompte(numeroCompte)
                            .orElseThrow(() -> new RuntimeException("Compte introuvable pour le numéro : " + numeroCompte));
    
                    // Vérifier si le crédit existe déjà
                    Optional<Credit> optionalCredit = creditRepository.findById(idCredit);
    
                    Credit credit;
                    if (optionalCredit.isPresent()) {
                        // Si le crédit existe, mettre à jour les informations
                        credit = optionalCredit.get();
                        credit.setFondDossier(fileLink);
                        credit.setCompte(compte);
                    } else {
                        // Si le crédit n'existe pas, créer un nouveau dossier de recouvrement
                        credit = new Credit();
                        credit.setIdCredit(idCredit);
                        credit.setFondDossier(fileLink);
                        credit.setCompte(compte);
                    }
    
                    // Sauvegarder le crédit (création ou mise à jour)
                    creditRepository.save(credit);
                } catch (Exception e) {
                    throw new Exception("Erreur ligne " + line + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new Exception("Erreur lors de la lecture du fichier: " + e.getMessage());
        }
    }

}
