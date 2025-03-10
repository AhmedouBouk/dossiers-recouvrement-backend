package com.bnm.recouvrement.service;


import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bnm.recouvrement.entity.Client;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.MiseEndemeur;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class MiseEnDemeurService {

    @Autowired
    private DossierRecouvrementService dossierService;
    
    public MiseEndemeur creerMiseEnDemeure(Long dossierId) {
        // Récupérer les informations du dossier
        Optional<DossierRecouvrement> dossierOpt = dossierService.getDossierById(dossierId);
        
        if (!dossierOpt.isPresent()) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + dossierId);
        }
        
        DossierRecouvrement dossier = dossierOpt.get();
        Compte compte = dossier.getCompte();
        Client client = compte.getClient();
        
        // Créer la mise en demeure
        MiseEndemeur miseEnDemeure = new MiseEndemeur();
        miseEnDemeure.setDossierId(dossierId);
        miseEnDemeure.setReference("MD-" + dossierId + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        miseEnDemeure.setDateCreation(LocalDate.now());
        miseEnDemeure.setNomClient(client.getNom());
        miseEnDemeure.setPrenomClient(client.getPrenom());
        miseEnDemeure.setAdresseClient(client.getAdresse());
        miseEnDemeure.setNumeroCompte(compte.getNomCompte());
        miseEnDemeure.setMontantDu(dossier.getEngagementTotal());
        miseEnDemeure.setStatus("GENEREE");
        
        // Générer contenu
        String contenu = genererContenuMiseEnDemeure(dossier, client);
        miseEnDemeure.setContenu(contenu);
        
        return miseEnDemeure;
    }
    
    public byte[] genererPdfMiseEnDemeure(Long dossierId) throws DocumentException {
        MiseEndemeur miseEnDemeure = creerMiseEnDemeure(dossierId);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // En-tête
        Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontGras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        
        // Logo et informations banque (à adapter)
        document.add(new Paragraph("BANQUE NATIONALE DE MAURITANIE", fontTitre));
        document.add(new Paragraph("Service de Recouvrement", fontGras));
        document.add(new Paragraph("Date : " + miseEnDemeure.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
        document.add(new Paragraph("Référence : " + miseEnDemeure.getReference(), fontNormal));
        document.add(new Paragraph("\n"));
        
        // Informations client
        document.add(new Paragraph("À l'attention de :", fontGras));
        document.add(new Paragraph(miseEnDemeure.getPrenomClient() + " " + miseEnDemeure.getNomClient(), fontNormal));
        document.add(new Paragraph(miseEnDemeure.getAdresseClient(), fontNormal));
        document.add(new Paragraph("\n\n"));
        
        // Objet
        document.add(new Paragraph("Objet : MISE EN DEMEURE - COMPTE N° " + miseEnDemeure.getNumeroCompte(), fontGras));
        document.add(new Paragraph("\n"));
        
        // Corps du document
        document.add(new Paragraph("Madame, Monsieur,", fontNormal));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Nous constatons avec regret que malgré nos relances précédentes, votre compte n° " 
                + miseEnDemeure.getNumeroCompte() + " présente toujours un solde débiteur de " 
                + String.format("%,.2f", miseEnDemeure.getMontantDu()) + " MRU.", fontNormal));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Par la présente, nous vous mettons en demeure de régulariser cette situation dans un délai de 15 jours à compter de la réception de ce courrier.", fontNormal));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("À défaut de paiement dans le délai imparti, nous nous verrons dans l'obligation d'engager toutes les procédures légales de recouvrement à votre encontre, ce qui pourrait entraîner des frais supplémentaires à votre charge.", fontNormal));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Nous vous prions de prendre contact avec notre service de recouvrement dans les plus brefs délais pour convenir des modalités de règlement de cette dette.", fontNormal));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Veuillez agréer, Madame, Monsieur, l'expression de nos salutations distinguées.", fontNormal));
        document.add(new Paragraph("\n\n"));
        document.add(new Paragraph("Le Service de Recouvrement", fontGras));
        document.add(new Paragraph("Banque Nationale de Mauritanie", fontNormal));
        
        document.close();
        
        return baos.toByteArray();
    }
    
    private String genererContenuMiseEnDemeure(DossierRecouvrement dossier, Client client) {
        // Exemple simple de contenu textuel
        StringBuilder sb = new StringBuilder();
        sb.append("MISE EN DEMEURE\n\n");
        sb.append("Client: ").append(client.getPrenom()).append(" ").append(client.getNom()).append("\n");
        sb.append("Compte: ").append(dossier.getCompte().getNomCompte()).append("\n");
        sb.append("Montant dû: ").append(String.format("%,.2f", dossier.getEngagementTotal())).append(" MRU\n\n");
        sb.append("Nous vous mettons en demeure de régler la somme due dans un délai de 15 jours.\n");
        
        return sb.toString();
    }
}