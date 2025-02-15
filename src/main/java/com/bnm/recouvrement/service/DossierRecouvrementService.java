package com.bnm.recouvrement.service;


import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.CreditRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.Credit;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DossierRecouvrementService {
     @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;

    public List<DossierRecouvrement> getIncompleteDossiers() {
        return dossierRecouvrementRepository.findByStatus("INCOMPLET");
    }
    public void detecterImpayesEtCreerDossiers(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
    
            // Ignorer la première ligne (en-tête)
            line = reader.readLine();
    
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String accountNumber = data[0];
                int impaye = Integer.parseInt(data[1]);
                String status = data[2];
                Long sold = Long.parseLong(data[3]);
                String PR = data[4];
                String PRP = data[5];
                float ajout = Float.parseFloat(data[6]);
                String encour = data[7];
                float principearemboursse = Float.parseFloat(data[8]);
    
                // Vérifier si le compte existe
                Compte compte = compteRepository.findById(accountNumber).orElse(null);
    
                if (compte == null) {
                    // Compte inexistant, afficher un message
                    System.out.println("Le compte avec le numéro " + accountNumber + " n'existe pas. Aucun dossier créé.");
                    continue; // Passer à la ligne suivante
                }
    
                // Vérifier si un DossierRecouvrement existe déjà pour ce compte
                Optional<DossierRecouvrement> existingDossier = dossierRecouvrementRepository.findByAccountNumber(accountNumber);
    
                if (existingDossier.isPresent()) {
                    // Si le dossier existe, mettre à jour uniquement l'impayé
                    DossierRecouvrement dossier = existingDossier.get();
                    dossier.setImpaye(impaye);
                    dossierRecouvrementRepository.save(dossier);
                    System.out.println("Le dossier de recouvrement pour le compte " + accountNumber + " a été mis à jour.");
                } else {
                    // Si le dossier n'existe pas, le créer
                    if (impaye > 6) {
                        DossierRecouvrement dossier = new DossierRecouvrement();
                        dossier.setAccountNumber(accountNumber);
                        dossier.setImpaye(impaye);
                        dossier.setStatus("Envoyé au recouvrement");
                        dossier.setDateCreation(LocalDateTime.now());
                        dossier.setSold(sold);
                        dossier.setPR(PR);
                        dossier.setPRP(PRP);
                        dossier.setAjout(ajout);
                        dossier.setEncour(encour);
                        dossier.setPrincipearemboursse(principearemboursse);
    
                        dossierRecouvrementRepository.save(dossier);
                        System.out.println("Un nouveau dossier de recouvrement a été créé pour le compte " + accountNumber + ".");
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Erreur lors de la détection des impayés : " + e.getMessage());
        }
    }
    
     @Autowired
    private CreditRepository creditRepository;

    // Ajouter un crédit dans un dossier de recouvrement
    @Transactional
    public DossierRecouvrement ajouterCredit(Long dossierId, Long creditId) {
        // Récupérer le dossier
        DossierRecouvrement dossier = dossierRecouvrementRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé pour l'id : " + dossierId));

        // Récupérer le crédit
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new RuntimeException("Crédit non trouvé pour l'id : " + creditId));

        // Établir la relation bidirectionnelle
        dossier.setCredit(credit);
        credit.setDossierRecouvrement(dossier);

        // Sauvegarder les modifications
        dossierRecouvrementRepository.save(dossier);
        creditRepository.save(credit);

        return dossier;
    }

    // Modifier un dossier de recouvrement
    public DossierRecouvrement modifierDossier(Long dossierId, DossierRecouvrement modifications) {
        DossierRecouvrement dossier = dossierRecouvrementRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé pour l'id : " + dossierId));

        dossier.setImpaye(modifications.getImpaye());
        dossier.setStatus(modifications.getStatus());
        dossier.setDateCreation(LocalDateTime.now());
        dossier.setAccountNumber(modifications.getAccountNumber());
        dossier.setSold(modifications.getSold());
        dossier.setPR(modifications.getPR());
        dossier.setPRP(modifications.getPRP());
        dossier.setAjout(modifications.getAjout());
        dossier.setEncour(modifications.getEncour());
        dossier.setPrincipearemboursse(modifications.getPrincipearemboursse());

        return dossierRecouvrementRepository.save(dossier);
    }

    // Modifier le statut d'un dossier
    public DossierRecouvrement modifierStatut(Long dossierId, String nouveauStatut) {
        DossierRecouvrement dossier = dossierRecouvrementRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé pour l'id : " + dossierId));
        dossier.setStatus(nouveauStatut);
        return dossierRecouvrementRepository.save(dossier);
    }

    // Supprimer un dossier de recouvrement
    public void supprimerDossier(Long dossierId) {
        if (!dossierRecouvrementRepository.existsById(dossierId)) {
            throw new RuntimeException("Dossier non trouvé pour l'id : " + dossierId);
        }
        dossierRecouvrementRepository.deleteById(dossierId);
    }

    // Lire un dossier par ID
    public Optional<DossierRecouvrement> lireDossier(Long dossierId) {
        return dossierRecouvrementRepository.findById(dossierId);
    }

    // Afficher tous les dossiers
    public List<DossierRecouvrement> afficherTousLesDossiers() {
        return dossierRecouvrementRepository.findAll();
    }
      public void sauvegarderDossierDansUnFichier(Long dossierId, String cheminDossier) {
    DossierRecouvrement dossier = dossierRecouvrementRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé pour l'id : " + dossierId));

    // Créer un fichier ZIP dans le répertoire spécifié
    String fichierZip = cheminDossier + "/dossier-" + dossierId + ".zip";

    try (FileOutputStream fos = new FileOutputStream(fichierZip);
         ZipOutputStream zos = new ZipOutputStream(fos)) {

        // Ajouter les informations principales sous forme de fichier texte
    

    } catch (IOException e) {
        throw new RuntimeException("Erreur lors de la création du fichier ZIP : " + e.getMessage(), e);
    }
}


   
    private void ajouterFichierAuZip(ZipOutputStream zos, String nomFichier, byte[] contenu) throws IOException {
        if (contenu != null) {
            ZipEntry entry = new ZipEntry(nomFichier);
            zos.putNextEntry(entry);
            zos.write(contenu);
            zos.closeEntry();
        }
    }
    public Optional<DossierRecouvrement> rechercherParAccountNumberExact(String accountNumber) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            return Optional.empty(); // Retourne vide si accountNumber est null ou vide
        }
        return dossierRecouvrementRepository.findByAccountNumber(accountNumber);
    }

    
    
    
}
