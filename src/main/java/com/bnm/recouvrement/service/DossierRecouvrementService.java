package com.bnm.recouvrement.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;

import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.repository.NotificationRepository;

@Service
public class DossierRecouvrementService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private CompteRepository compteRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private HistoryService historyService;
    @Autowired

     private NotificationRepository notificationRepository;
    public List<DossierRecouvrement> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public Optional<DossierRecouvrement> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Transactional
    public DossierRecouvrement createDossier(DossierRecouvrement dossier) {
        dossier = dossierRepository.save(dossier);
        
        // Enregistrer l'événement de création dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logCreate(
            username, 
            "dossier", 
            dossier.getId().toString(), 
            "Dossier #" + dossier.getId()
        );
        
        // Vérifier si le dossier nécessite un fichier de garantie
        if (dossier.getGarantiesValeur() != null && !dossier.getGarantiesValeur().isEmpty() 
                && dossier.getGarantiesFile() == null) {
            System.out.println("Condition remplie pour envoyer une notification de garantie pour le dossier #" + dossier.getId());
            // Envoyer notification pour le téléchargement de garantie
            notificationService.notifyGarantieUploadRequired(dossier);
        } else {
            System.out.println("Condition NON remplie pour envoyer une notification de garantie pour le dossier #" + dossier.getId());
            System.out.println("- Garanties Valeur: " + (dossier.getGarantiesValeur() != null ? dossier.getGarantiesValeur() : "null"));
            System.out.println("- Garanties File: " + (dossier.getGarantiesFile() != null ? dossier.getGarantiesFile() : "null"));
        }
        
        // Vérifier si le dossier a une référence de chèque
        if (dossier.getReferencesChecks() != null && !dossier.getReferencesChecks().isEmpty() 
                && dossier.getChequeFile() == null) {
            System.out.println("Condition remplie pour envoyer une notification de chèque pour le dossier #" + dossier.getId());
            // Envoyer notification pour le téléchargement de chèque
            notificationService.notifyChequeUploadRequired(dossier);
        } else {
            System.out.println("Condition NON remplie pour envoyer une notification de chèque pour le dossier #" + dossier.getId());
            System.out.println("- References Checks: " + (dossier.getReferencesChecks() != null ? dossier.getReferencesChecks() : "null"));
            System.out.println("- Cheque File: " + (dossier.getChequeFile() != null ? dossier.getChequeFile() : "null"));
        }
        
        // Vérifier si le dossier a des références de documents DO (caution, LC, crédit)
        boolean hasDoRefs = (dossier.getReferencesCautions() != null && !dossier.getReferencesCautions().isEmpty()) ||
                           (dossier.getReferencesLC() != null && !dossier.getReferencesLC().isEmpty()) ||
                           (dossier.getReferencesCredits() != null && !dossier.getReferencesCredits().isEmpty());
                           
        if (hasDoRefs) {
            System.out.println("Condition remplie pour envoyer une notification documents DO pour le dossier #" + dossier.getId());
            // Envoyer notification pour le téléchargement des documents DO
            notificationService.notifyDoDocumentsUploadRequired(dossier);
        } else {
            System.out.println("Condition NON remplie pour envoyer une notification documents DO pour le dossier #" + dossier.getId());
            System.out.println("- References Cautions: " + (dossier.getReferencesCautions() != null ? dossier.getReferencesCautions() : "null"));
            System.out.println("- References LC: " + (dossier.getReferencesLC() != null ? dossier.getReferencesLC() : "null"));
            System.out.println("- References Credits: " + (dossier.getReferencesCredits() != null ? dossier.getReferencesCredits() : "null"));
        }
        
        return dossier;
    }

    @Transactional
    public DossierRecouvrement updateDossier(Long id, DossierRecouvrement dossier) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
        dossier.setId(id);
        DossierRecouvrement updatedDossier = dossierRepository.save(dossier);
        
        // Enregistrer l'événement de mise à jour dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logUpdate(
            username, 
            "dossier", 
            id.toString(), 
            "Dossier #" + id
        );
        
        return updatedDossier;
    }

    @Transactional
    public void deleteDossier(Long id) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
        
        // Enregistrer l'événement de suppression dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logDelete(
            username, 
            "dossier", 
            id.toString(), 
            "Dossier #" + id
        );
        // Supprimer les notifications liées au dossier
notificationRepository.deleteByDossierId(id);

        
        dossierRepository.deleteById(id);
    }

    public List<DossierRecouvrement> searchDossiers(Long dossierId, String numeroCompte, String nomClient) {
        if (dossierId != null) {
            return dossierRepository.findById(dossierId)
                    .map(List::of)
                    .orElse(new ArrayList<>());
        } else if (numeroCompte != null) {
            return dossierRepository.findByCompteNomCompte(numeroCompte);
        } else if (nomClient != null) {
            return dossierRepository.findByCompteClientNomContainingIgnoreCase(nomClient);
        }
        return new ArrayList<>();
    }

    @Transactional
    public int importDossiersFromFile(String filePath) throws Exception {
        int importCount = 0;
        List<DossierRecouvrement> dossiersRequiringGaranties = new ArrayList<>();
        List<DossierRecouvrement> dossiersRequiringCheques = new ArrayList<>();
        List<DossierRecouvrement> dossiersRequiringDoDocuments = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
    
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer la première ligne (en-têtes)
                }
    
                String[] data = line.split(";"); // Utiliser ';' comme séparateur
                if (data.length < 14) {
                    throw new Exception("Format de ligne invalide: " + line);
                }
    
                // Numéro de compte (ne pas convertir en double)
                String numeroCompte = data[0].trim();
    
                // Vérifier si le numéro de compte existe
                Optional<Compte> compteOpt = compteRepository.findById(numeroCompte);
                if (compteOpt.isEmpty()) {
                    System.out.println("Compte non trouvé, ligne ignorée: " + numeroCompte);
                    continue; // Ignorer cette ligne et passer à la suivante
                }
    
                // Récupérer ou créer un dossier
                List<DossierRecouvrement> dossiers = dossierRepository.findByCompteNomCompte(numeroCompte);
                DossierRecouvrement dossier = dossiers.isEmpty() ? new DossierRecouvrement() : dossiers.get(0);
    
                // Remplir les champs du dossier
                dossier.setCompte(compteOpt.get());
                dossier.setEngagementTotal(data[3] != null && !data[3].trim().isEmpty() ? parseDoubleOrNull(data[3]) : null);
                dossier.setMontantPrincipal(data[4] != null && !data[4].trim().isEmpty() ? parseDoubleOrNull(data[4]) : null);
                dossier.setInteretContractuel(data[5] != null && !data[5].trim().isEmpty() ? parseDoubleOrNull(data[5]) : null);
                dossier.setInteretRetard(data[6] != null && !data[6].trim().isEmpty() ? parseDoubleOrNull(data[6]) : null);
                dossier.setAgenceOuvertureCompte(data[8] != null && !data[8].trim().isEmpty() ? data[8].trim() : null);
                dossier.setReferencesChecks(data[9] != null && !data[9].trim().isEmpty() ? data[9].trim() : null);
                dossier.setReferencesCredits(data[10] != null && !data[10].trim().isEmpty() ? data[10].trim() : null);
                dossier.setReferencesCautions(data[11] != null && !data[11].trim().isEmpty() ? data[11].trim() : null);
                dossier.setReferencesLC(data[12] != null && !data[12].trim().isEmpty() ? data[12].trim() : null);
                dossier.setProvision(data[13] != null && !data[13].trim().isEmpty() ? parseDoubleOrNull(data[13]) : null);
                dossier.setGarantiesValeur(data[14] != null && !data[14].trim().isEmpty() ? data[14].trim() : null);
    
                // Gérer les natures d'engagement
                dossier.setNaturesEngagement(data[7]);
    
                // Ajouter la date de création
                dossier.setDateCreation(LocalDateTime.now());
    
                // Vérifier si le dossier nécessite un fichier de garantie
               // Vérifier si le dossier nécessite un fichier de garantie
               if (dossier.getGarantiesValeur() != null && !dossier.getGarantiesValeur().isEmpty() 
               && dossier.getGarantiesFile() == null) {
           System.out.println("Dossier #" + dossier.getId() + " nécessite une garantie - Préparation de notification");
           dossiersRequiringGaranties.add(dossier);
       }
       
       // Vérifier si le dossier a une référence de chèque
       if (dossier.getReferencesChecks() != null && !dossier.getReferencesChecks().isEmpty() 
               && dossier.getChequeFile() == null) {
           System.out.println("Dossier #" + dossier.getId() + " nécessite un chèque - Préparation de notification");
           dossiersRequiringCheques.add(dossier);
       }
       
       // Vérifier si le dossier a des références de documents DO
       boolean hasDoRefs = (dossier.getReferencesCautions() != null && !dossier.getReferencesCautions().isEmpty() 
                           && dossier.getCautionsFile() == null) ||
                          (dossier.getReferencesLC() != null && !dossier.getReferencesLC().isEmpty() 
                           && dossier.getLcFile() == null) ||
                          (dossier.getReferencesCredits() != null && !dossier.getReferencesCredits().isEmpty() 
                           && dossier.getCreditsFile() == null);
                          
       if (hasDoRefs) {
           System.out.println("Dossier #" + dossier.getId() + " nécessite des documents DO - Préparation de notification");
           dossiersRequiringDoDocuments.add(dossier);
       }
       
       // Sauvegarder le dossier
       dossierRepository.save(dossier);
       importCount++;
   }
   
   // Enregistrer l'événement d'import dans l'historique
   if (importCount > 0) {
       // Récupérer l'utilisateur actuel
       Authentication auth = SecurityContextHolder.getContext().getAuthentication();
       String username = auth.getName();
       
       // Créer un événement d'historique
       historyService.logImport(
           username, 
           new File(filePath).getName(), 
           "Import de " + importCount + " dossiers réussi"
       );
       
       System.out.println("Historique d'import créé pour " + importCount + " dossiers par " + username);
   }
}

// Envoyer des notifications pour tous les dossiers nécessitant des garanties
System.out.println("Nombre de dossiers nécessitant des garanties: " + dossiersRequiringGaranties.size());
for (DossierRecouvrement dossier : dossiersRequiringGaranties) {
   System.out.println("Envoi de notification garantie pour le dossier #" + dossier.getId());
   notificationService.notifyGarantieUploadRequired(dossier);
}

// Envoyer des notifications pour tous les dossiers nécessitant des chèques
System.out.println("Nombre de dossiers nécessitant des chèques: " + dossiersRequiringCheques.size());
for (DossierRecouvrement dossier : dossiersRequiringCheques) {
   System.out.println("Envoi de notification chèque pour le dossier #" + dossier.getId());
   notificationService.notifyChequeUploadRequired(dossier);
}

// Envoyer des notifications pour tous les dossiers nécessitant des documents DO
System.out.println("Nombre de dossiers nécessitant des documents DO: " + dossiersRequiringDoDocuments.size());
for (DossierRecouvrement dossier : dossiersRequiringDoDocuments) {
   System.out.println("Envoi de notification documents DO pour le dossier #" + dossier.getId());
   notificationService.notifyDoDocumentsUploadRequired(dossier);
}

return importCount;
}

private Double parseDoubleOrNull(String value) {
try {
   return value != null && !value.trim().isEmpty() ? Double.parseDouble(value.trim()) : null;
} catch (NumberFormatException e) {
   return null;
}
}

@Transactional
public void updateChequeFile(Long dossierId, String chequeFileUrl) {
// Récupérer le dossier par son ID
DossierRecouvrement dossier = dossierRepository.findById(dossierId)
       .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID : " + dossierId));

// Mettre à jour le champ chequeFile
dossier.setChequeFile(chequeFileUrl);

// Sauvegarder les modifications dans la base de données
dossierRepository.save(dossier);

// Enregistrer l'événement dans l'historique
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();

historyService.createEvent(
   username,
   "update", 
   "cheque", 
   dossierId.toString(), 
   "Dossier #" + dossierId,
   "Mise à jour URL du fichier chèque: " + chequeFileUrl
);
}

// Sauvegarder un dossier
public DossierRecouvrement saveDossier(DossierRecouvrement dossier) {
DossierRecouvrement savedDossier = dossierRepository.save(dossier);

// Enregistrer l'événement dans l'historique
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();

historyService.createEvent(
   username,
   "save", 
   "dossier", 
   savedDossier.getId().toString(), 
   "Dossier #" + savedDossier.getId(),
   "Sauvegarde du dossier"
);

return savedDossier;
}

/**
 * Met à jour le statut d'un dossier et enregistre l'événement dans l'historique
 * @param dossierId ID du dossier à mettre à jour
 * @param newStatus Nouveau statut du dossier
 * @return Le dossier mis à jour
 */
@Transactional
public DossierRecouvrement updateDossierStatus(Long dossierId, String newStatus) {
    // Récupérer le dossier par son ID
    DossierRecouvrement dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec l'ID : " + dossierId));
    
    // Sauvegarder l'ancien statut pour l'historique
    String oldStatus = dossier.getStatus();
    
    // Mettre à jour le statut
    dossier.setStatus(newStatus);
    
    // Sauvegarder les modifications
    DossierRecouvrement updatedDossier = dossierRepository.save(dossier);
    
    // Enregistrer l'événement dans l'historique
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String username = auth.getName();
    
    historyService.createEvent(
        username,
        "update", 
        "status", 
        dossierId.toString(), 
        "Dossier #" + dossierId,
        "Mise à jour du statut: " + (oldStatus != null ? oldStatus : "Non défini") + " → " + newStatus
    );
    
    return updatedDossier;
}

}