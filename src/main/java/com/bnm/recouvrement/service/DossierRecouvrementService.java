package com.bnm.recouvrement.service;

import java.io.BufferedReader;
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
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;

import java.io.IOException;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.DossierRecouvrement;

@Service
public class DossierRecouvrementService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private CompteRepository compteRepository;

    public List<DossierRecouvrement> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public Optional<DossierRecouvrement> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Transactional
    public DossierRecouvrement createDossier(DossierRecouvrement dossier) {
        return dossierRepository.save(dossier);
    }

    @Transactional
    public DossierRecouvrement updateDossier(Long id, DossierRecouvrement dossier) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
        dossier.setId(id);
        return dossierRepository.save(dossier);
    }

    @Transactional
    public void deleteDossier(Long id) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
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
    
                // Sauvegarder le dossier
                dossierRepository.save(dossier);
                importCount++;
            }
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
}

// Sauvegarder un dossier
public DossierRecouvrement saveDossier(DossierRecouvrement dossier) {
    return dossierRepository.save(dossier);
}


 public ResponseEntity<Resource> generateMiseEnDemeurePdf(Long dossierId) {
        Optional<DossierRecouvrement> dossierOpt = dossierRepository.findById(dossierId);
        if (dossierOpt.isEmpty()) {
            throw new IllegalArgumentException("Dossier non trouvé !");
        }

        DossierRecouvrement dossier = dossierOpt.get();

        // Créer un fichier temporaire pour stocker le PDF
        File tempFile;
        try {
            tempFile = File.createTempFile("mise_en_demeure_", ".pdf");

            // Créer un document PDF
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(tempFile));
            document.open();

            // Définir une police
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font textFont = new Font(Font.FontFamily.HELVETICA, 12);

            // Ajouter le titre
            Paragraph title = new Paragraph("Mise en Demeure", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Ajouter les détails du dossier
            document.add(new Paragraph("Référence dossier : " + dossier.getId(), textFont));
            document.add(new Paragraph("Client : " + dossier.getCompte().getNomCompte(), textFont));
            document.add(new Paragraph("Montant dû : " + dossier.getMontantPrincipal() + " €", textFont));
            document.add(new Paragraph("Date de création : " + dossier.getDateCreation(), textFont));
            document.add(new Paragraph("\n"));

            // Ajouter le texte de mise en demeure
            document.add(new Paragraph("Madame, Monsieur,", textFont));
            document.add(new Paragraph("Par la présente, nous vous mettons en demeure de payer la somme de "
                    + dossier.getMontantPrincipal() + " € sous 15 jours.", textFont));
            document.add(new Paragraph("À défaut, nous engagerons des poursuites judiciaires.", textFont));
            document.add(new Paragraph("\nCordialement,", textFont));

            document.close();

            // Utiliser un FileSystemResource pour servir le fichier
            FileSystemResource resource = new FileSystemResource(tempFile);

            // Retourner le fichier en tant que réponse HTTP
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mise_en_demeure.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}

